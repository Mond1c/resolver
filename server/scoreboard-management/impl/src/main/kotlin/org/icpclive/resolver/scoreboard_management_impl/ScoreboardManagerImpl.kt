package org.icpclive.resolver.scoreboard_management_impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.TeamId
import org.icpclive.resolver.Direction
import org.icpclive.resolver.ServerToControllerMessage
import org.icpclive.resolver.State
import org.icpclive.resolver.VariantToGoto
import org.icpclive.resolver.resolution_logic_api.ResolutionStep
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManagerOptions
import org.icpclive.resolver.scoreboard_management_api.UiEvent
import org.icpclive.resolver.scoreboard_management_api.UiMapper
import org.icpclive.resolver.scoreboard_management_api.toCore
import org.icpclive.resolver.util_api.ScoreboardCalculator
import org.icpclive.resolver.util_api.exception.CoreExceptions
import org.icpclive.resolver.util_api.exception.UnexpectedStateException

@ExperimentalCoroutinesApi
@Suppress("DuplicatedCode")
class ScoreboardManagerImpl(
    private val frozenState: ContestState,
    private val snapshots: List<ContestState>,
    private val calculator: ScoreboardCalculator,
    steps: List<ResolutionStep>,
    private val uiMapper: UiMapper,
    scoreboardManagerOptions: ScoreboardManagerOptions,
    private val scoreboardCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ScoreboardManager(scoreboardManagerOptions) {
    private data class LastChosenRowInfo(
        val indexOfLastChosenRow: Int?,
        val teamOfLastChosenRow: TeamId?,
        val isLastChosenRowChosenNow: Boolean
    ) {
        companion object {
            val DEFAULT = LastChosenRowInfo(
                indexOfLastChosenRow = null,
                teamOfLastChosenRow = null,
                isLastChosenRowChosenNow = false
            )
        }
    }

    private data class GotoQuery(
        val stateIndex: Int,
        val teamId: TeamId
    )

    private val uiEvents = mutableListOf<UiEvent>()
    private val currentState = MutableStateFlow(frozenState)

    init {
        uiEvents.addAll(uiMapper mapToUiEvents steps)
    }

    private val settings =
        MutableStateFlow(
            ServerToControllerMessage.ScoreboardManagerSettings.provideDefault(
                scoreboardManagerOptions.isGotoEnabled
            )
        )
    private val MutableStateFlow<ServerToControllerMessage.ScoreboardManagerSettings>.isUp
        get() = value.direction == Direction.UP
    private val MutableStateFlow<ServerToControllerMessage.ScoreboardManagerSettings>.isStopped
        get() = value.state == State.STOP

    private val timeBetween = MutableStateFlow(scoreboardManagerOptions.baseTimeBetweenMs)
    private val upSignal = MutableSharedFlow<Unit>()
    private val downSignal = MutableSharedFlow<Unit>()
    private val gotoSignal = MutableSharedFlow<GotoQuery>()
    private val scoreboardManagerScope =
        CoroutineScope(SupervisorJob() + scoreboardCoroutineDispatcher)
    private val mtx = Mutex()
    private var currentUnusedUiEventsIndex = 0
    private var currentUnusedSnapshotsIndex = 0
    private val lastChosenRowInfo = MutableStateFlow(LastChosenRowInfo.DEFAULT)

    override fun start() {
        settings.update {
            it.copy(state = State.PROCESS)
        }
    }

    override fun stop() {
        settings.update {
            it.copy(state = State.STOP)
        }
    }

    override fun changeDirection() {
        settings.update {
            it.copy(direction = if (it.direction == Direction.DOWN) Direction.UP else Direction.DOWN)
        }
    }

    override fun applySpeedFactor(speedFactor: Double) {
        if (speedFactor <= 0) {
            return
        }
        timeBetween.update {
            (scoreboardManagerOptions.baseTimeBetweenMs / speedFactor).toLong()
        }
        settings.update {
            it.copy(speedFactor = speedFactor)
        }
    }

    override fun up() {
        scoreboardManagerScope.launch {
            upSignal.emit(Unit)
        }
    }

    override fun down() {
        scoreboardManagerScope.launch {
            downSignal.emit(Unit)
        }
    }

    override fun getUiEventsFlow(): Flow<UiEvent> {
        return flowOf(
            getUpFlow(),
            getDownFlow(),
            getAutoUpFlow(),
            getAutoDownFlow(),
            getGotoFlow()
        )
            .flattenMerge()
            .flowOn(scoreboardCoroutineDispatcher)
    }

    override fun getScoreboard(): UiEvent.Scoreboard {
        val (indexOfLastChosenRow, teamOfLastChosenRow, isLastChosenRowChosenNow) = lastChosenRowInfo.value
        val (rows, ranking) = calculator.calculateScoreboard(currentState.value)
            ?: throw CoreExceptions.contestInfoIsNullException
        return UiEvent.Scoreboard(
            teamIdToScoreboardRow = rows,
            order = ranking.order,
            ranks = ranking.ranks,
            contestInfo = currentState.value.infoAfterEvent
                ?: throw CoreExceptions.contestInfoIsNullException,
            indexOfLastChosenRow = indexOfLastChosenRow,
            teamOfLastChosenRow = teamOfLastChosenRow,
            isLastChosenRowChosenNow = isLastChosenRowChosenNow
        )
    }

    override fun getCountOfProblems(): Int {
        return currentState.value.infoAfterEvent!!.problems.keys.size
    }

    override fun goto(stateIndex: Int, teamId: TeamId) {
        scoreboardManagerScope.launch {
            gotoSignal.emit(GotoQuery(stateIndex, teamId))
        }
    }

    override fun getVariantsToGoto(teamId: TeamId): ServerToControllerMessage.VariantsToGoto? {
        if (!settings.value.isGotoEnabled) {
            return null
        }
        val variants = mutableListOf<VariantToGoto>()
        val fullName = currentState.value.infoAfterEvent?.teams[teamId]?.fullName ?: return null
        var stateIndex = -1
        for (i in 0..<uiEvents.size) {
            val uiEvent = uiEvents[i]
            if (uiEvent.isImportant()) {
                stateIndex++
            }
            if (uiEvent is UiEvent.ChooseRow && uiEvent.teamId == teamId) {
                val problemsToResolveDisplayNames = mutableListOf<String>()
                when (val next = uiEvents[i + 1]) {
                    is UiEvent.ChooseProblem -> {
                        problemsToResolveDisplayNames.add(
                            currentState.value.getProblemDisplayName(
                                next.problemId
                            )
                        )
                        var j = i + 2
                        while (j < uiEvents.size && uiEvents[j] !is UiEvent.UnchooseRow) {
                            val nextNext = uiEvents[j]
                            if (nextNext is UiEvent.ChooseProblem) {
                                problemsToResolveDisplayNames.add(
                                    currentState.value.getProblemDisplayName(nextNext.problemId)
                                )
                            }
                            j++
                        }
                        variants.add(VariantToGoto(stateIndex, problemsToResolveDisplayNames))
                    }

                    is UiEvent.ShowTeamAwards, is UiEvent.UnchooseRow -> {
                        variants.add(VariantToGoto(stateIndex, problemsToResolveDisplayNames))
                    }

                    else -> throw CoreExceptions.badUiEventsSequenceException
                }
            }
        }
        return ServerToControllerMessage.VariantsToGoto(teamId.toCore(), fullName, variants)
    }

    override fun getSettingsFlow(): StateFlow<ServerToControllerMessage.ScoreboardManagerSettings> {
        return settings.asStateFlow()
    }

    private fun getGotoFlow(): Flow<UiEvent> {
        return flow {
            gotoSignal.collect { (stateIndex, teamId) ->
                if (!settings.value.isGotoEnabled) {
                    return@collect
                }
                mtx.withLock {
                    if (uiEvents.isEmpty()) {
                        return@withLock
                    }
                    stop()
                    val resultIndex = uiEvents.findFirstChooseRow(stateIndex, teamId)
                    repeat(scoreboardManagerOptions.replay) {
                        emit(UiEvent.NoOp)
                    }
                    if (currentUnusedUiEventsIndex > 0 &&
                        (uiEvents[currentUnusedUiEventsIndex - 1] is UiEvent.ChooseProblem ||
                                uiEvents[currentUnusedUiEventsIndex - 1] is UiEvent.ShowTeamAwards ||
                                uiEvents[currentUnusedUiEventsIndex - 1] is UiEvent.ShowGroupAwards)
                    ) {
                        emit(uiMapper reverse uiEvents[currentUnusedUiEventsIndex - 1])
                    }
                    if (currentUnusedUiEventsIndex > 0 &&
                        (uiEvents[currentUnusedUiEventsIndex - 1] is UiEvent.UnchooseProblem ||
                                uiEvents[currentUnusedUiEventsIndex - 1] is UiEvent.HideTeamAwards ||
                                uiEvents[currentUnusedUiEventsIndex - 1] is UiEvent.HideGroupAwards)
                    ) {
                        emit(uiEvents[currentUnusedUiEventsIndex - 1])
                    }
                    if (currentUnusedUiEventsIndex > 1 &&
                        (uiEvents[currentUnusedUiEventsIndex - 2] is UiEvent.ChooseProblem ||
                                uiEvents[currentUnusedUiEventsIndex - 2] is UiEvent.ShowTeamAwards ||
                                uiEvents[currentUnusedUiEventsIndex - 2] is UiEvent.ShowGroupAwards)
                    ) {
                        emit(uiMapper reverse uiEvents[currentUnusedUiEventsIndex - 2])
                    }
                    if (currentUnusedUiEventsIndex > 1 &&
                        (uiEvents[currentUnusedUiEventsIndex - 2] is UiEvent.UnchooseProblem ||
                                uiEvents[currentUnusedUiEventsIndex - 2] is UiEvent.HideTeamAwards ||
                                uiEvents[currentUnusedUiEventsIndex - 2] is UiEvent.HideGroupAwards)
                    ) {
                        emit(uiEvents[currentUnusedUiEventsIndex - 2])
                    }
                    if (stateIndex == -1) {
                        handleLastChosenRow(uiEvents[0])
                        currentState.update {
                            frozenState
                        }
                        currentUnusedSnapshotsIndex = 0
                        currentUnusedUiEventsIndex = 1
                        emit(getScoreboard())
                        repeat(scoreboardManagerOptions.replay) {
                            emit(UiEvent.NoOp)
                        }
                        emit(uiEvents[0])
                    } else if (resultIndex == null) {
                        handleLastChosenRow(uiEvents.findLast { it is UiEvent.UnchooseRow }
                            ?: throw UnexpectedStateException())
                        currentState.update {
                            snapshots.lastOrNull() ?: frozenState
                        }
                        currentUnusedSnapshotsIndex = snapshots.size
                        currentUnusedUiEventsIndex = uiEvents.size
                        emit(getScoreboard())
                        repeat(scoreboardManagerOptions.replay) {
                            emit(UiEvent.NoOp)
                        }
                    } else {
                        handleLastChosenRow(uiEvents[resultIndex])
                        currentState.update {
                            snapshots[stateIndex]
                        }
                        currentUnusedSnapshotsIndex = stateIndex + 1
                        currentUnusedUiEventsIndex = resultIndex + 1
                        emit(getScoreboard())
                        repeat(scoreboardManagerOptions.replay) {
                            emit(UiEvent.NoOp)
                        }
                        emit(uiEvents[resultIndex])
                    }
                }
            }
        }
    }

    private fun getUpFlow(): Flow<UiEvent> {
        return flow {
            upSignal.collect {
                mtx.withLock {
                    if (settings.value.state == State.STOP && currentUnusedUiEventsIndex < uiEvents.size) {
                        if (uiEvents[currentUnusedUiEventsIndex].isImportant()) {
                            currentState.update {
                                snapshots[currentUnusedSnapshotsIndex]
                            }
                            currentUnusedSnapshotsIndex++
                        }
                        handleLastChosenRow(uiEvents[currentUnusedUiEventsIndex])
                        emit(uiEvents[currentUnusedUiEventsIndex])
                        currentUnusedUiEventsIndex++
                    }
                }
            }
        }
    }

    private fun getDownFlow(): Flow<UiEvent> {
        return flow {
            downSignal.collect {
                mtx.withLock {
                    if (settings.isStopped && currentUnusedUiEventsIndex > 0) {
                        currentUnusedUiEventsIndex--
                        if (uiEvents[currentUnusedUiEventsIndex].isImportant()) {
                            currentUnusedSnapshotsIndex--
                            currentState.update {
                                snapshots[currentUnusedSnapshotsIndex]
                            }
                        }
                        val uiEvent = uiMapper reverse uiEvents[currentUnusedUiEventsIndex]
                        handleLastChosenRow(uiEvent)
                        emit(uiEvent)
                    }
                }
            }
        }
    }

    private fun getAutoUpFlow(): Flow<UiEvent> {
        return flow {
            while (true) {
                if (settings.isStopped || !settings.isUp) {
                    delay(timeBetween.value)
                    continue
                }
                mtx.withLock {
                    if (!settings.isStopped && settings.isUp && currentUnusedUiEventsIndex < uiEvents.size) {
                        if (uiEvents[currentUnusedUiEventsIndex].isImportant()) {
                            currentState.update {
                                snapshots[currentUnusedSnapshotsIndex]
                            }
                            currentUnusedSnapshotsIndex++
                        }
                        handleLastChosenRow(uiEvents[currentUnusedUiEventsIndex])
                        emit(uiEvents[currentUnusedUiEventsIndex])
                        currentUnusedUiEventsIndex++
                    }
                    delay(timeBetween.value)
                }
            }
        }
    }

    private fun getAutoDownFlow(): Flow<UiEvent> {
        return flow {
            while (true) {
                if (settings.isStopped || settings.isUp) {
                    delay(timeBetween.value)
                    continue
                }
                mtx.withLock {
                    if (!settings.isStopped && !settings.isUp && currentUnusedUiEventsIndex > 0) {
                        currentUnusedUiEventsIndex--
                        if (uiEvents[currentUnusedUiEventsIndex].isImportant()) {
                            currentUnusedSnapshotsIndex--
                            currentState.update {
                                snapshots[currentUnusedSnapshotsIndex]
                            }
                        }
                        val uiEvent = uiMapper reverse uiEvents[currentUnusedUiEventsIndex]
                        handleLastChosenRow(uiEvent)
                        emit(uiEvent)
                    }
                    delay(timeBetween.value)
                }
            }
        }
    }

    private fun handleLastChosenRow(uiEvent: UiEvent) {
        lastChosenRowInfo.update {
            when (uiEvent) {
                is UiEvent.ChooseRow -> {
                    LastChosenRowInfo(
                        indexOfLastChosenRow = uiEvent.index,
                        teamOfLastChosenRow = uiEvent.teamId,
                        isLastChosenRowChosenNow = true
                    )
                }

                is UiEvent.UnchooseRow -> {
                    LastChosenRowInfo(
                        indexOfLastChosenRow = uiEvent.index,
                        teamOfLastChosenRow = uiEvent.teamId,
                        isLastChosenRowChosenNow = false
                    )
                }

                is UiEvent.ShowGroupAwards -> {
                    stop()
                    it
                }

                else -> {
                    it
                }
            }
        }
    }
}
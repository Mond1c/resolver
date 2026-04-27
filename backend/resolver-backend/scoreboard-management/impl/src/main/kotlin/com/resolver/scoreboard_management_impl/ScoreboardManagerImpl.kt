package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.ScoreboardManagerOptions
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_api.UiMapper
import com.resolver.util_api.ScoreboardCalculator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.TeamId

@ExperimentalCoroutinesApi
@Suppress("DuplicatedCode")
class ScoreboardManagerImpl(
    frozenState: ContestState,
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

    private val uiEvents = mutableListOf<UiEvent>()
    private val currentState = MutableStateFlow(frozenState)

    init {
        uiEvents.addAll(uiMapper mapToUiEvents steps)
    }

    private val timeBetween = MutableStateFlow(scoreboardManagerOptions.baseTimeBetweenMs)
    private val isUp = MutableStateFlow(true)
    private val isStopped = MutableStateFlow(true)
    private val upSignal = MutableSharedFlow<Unit>()
    private val downSignal = MutableSharedFlow<Unit>()
    private val scoreboardManagerScope = CoroutineScope(SupervisorJob() + scoreboardCoroutineDispatcher)
    private val mtx = Mutex()
    private var currentUnusedUiEventsIndex = 0
    private var currentUnusedSnapshotsIndex = 0
    private val lastChosenRowInfo = MutableStateFlow(LastChosenRowInfo.DEFAULT)

    override fun start() {
        isStopped.update {
            false
        }
    }

    override fun stop() {
        isStopped.update {
            true
        }
    }

    override fun changeDirection() {
        isUp.update { currentIsUp ->
            !currentIsUp
        }
    }

    override fun applySpeedFactor(speedFactor: Double) {
        timeBetween.update {
            (scoreboardManagerOptions.baseTimeBetweenMs / speedFactor).toLong()
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
            getAutoDownFlow()
        )
            .flattenMerge()
            .flowOn(scoreboardCoroutineDispatcher)
    }

    override fun getScoreboard(): UiEvent.Scoreboard {
        val (indexOfLastChosenRow, teamOfLastChosenRow, isLastChosenRowChosenNow) = lastChosenRowInfo.value
        val (rows, ranking) = calculator.calculateScoreboard(currentState.value) ?: TODO("Unexpected null")
        return UiEvent.Scoreboard(
            teamIdToScoreboardRow = rows,
            order = ranking.order,
            ranks = ranking.ranks,
            contestInfo = currentState.value.infoAfterEvent!!,
            indexOfLastChosenRow = indexOfLastChosenRow,
            teamOfLastChosenRow = teamOfLastChosenRow,
            isLastChosenRowChosenNow = isLastChosenRowChosenNow
        )
    }

    override fun getCountOfProblems(): Int {
        return currentState.value.infoAfterEvent!!.problems.keys.size
    }

    private fun getUpFlow(): Flow<UiEvent> {
        return flow {
            upSignal.collect {
                mtx.withLock {
                    if (isStopped.value && currentUnusedUiEventsIndex < uiEvents.size) {
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
                    if (isStopped.value && currentUnusedUiEventsIndex > 0) {
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
                if (isStopped.value || !isUp.value) {
                    delay(timeBetween.value)
                    continue
                }
                mtx.withLock {
                    if (!isStopped.value && isUp.value && currentUnusedUiEventsIndex < uiEvents.size) {
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
                if (isStopped.value || isUp.value) {
                    delay(timeBetween.value)
                    continue
                }
                mtx.withLock {
                    if (!isStopped.value && !isUp.value && currentUnusedUiEventsIndex > 0) {
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

                else -> {
                    it
                }
            }
        }
    }
}
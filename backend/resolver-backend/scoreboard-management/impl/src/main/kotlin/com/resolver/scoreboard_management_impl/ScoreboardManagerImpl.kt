package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_api.UiMapper
import com.resolver.util_api.ScoreboardCalculator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ScoreboardRow

@ExperimentalCoroutinesApi
class ScoreboardManagerImpl(
    frozenState: ContestState,
    private val snapshots: List<ContestState>,
    private val calculator: ScoreboardCalculator,
    steps: List<ResolutionStep>,
    private val uiMapper: UiMapper,
    private val scoreboardCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ScoreboardManager {
    private val uiEvents = mutableListOf<UiEvent>()
    private val currentState = MutableStateFlow(frozenState)

    init {
        uiEvents.addAll(uiMapper mapToUiEvents steps)
    }

    private val timeBetween = MutableStateFlow(ScoreboardManager.BASE_TIME_BETWEEN_MS)
    private val isUp = MutableStateFlow(true)
    private val isStopped = MutableStateFlow(true)
    private val upSignal = MutableSharedFlow<Unit>()
    private val downSignal = MutableSharedFlow<Unit>()
    private val scoreboardManagerScope = CoroutineScope(SupervisorJob() + scoreboardCoroutineDispatcher)
    private val mtx = Mutex()
    private var currentUnusedUiEventsIndex = 0
    private var currentUnusedSnapshotsIndex = 0

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
            (ScoreboardManager.BASE_TIME_BETWEEN_MS * speedFactor).toLong()
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

    override fun getScoreboard(): List<ScoreboardRow> {
        val (rows, ranking) = calculator.calculateScoreboard(currentState.value) ?: TODO("Unexpected null")
        return ranking.order.map { rows[it] ?: TODO("Unexpected null") }
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
                        emit(uiEvents[currentUnusedUiEventsIndex])
                        currentUnusedUiEventsIndex++
                        delay(timeBetween.value)
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
                        emit(uiMapper reverse uiEvents[currentUnusedUiEventsIndex])
                        delay(timeBetween.value)
                    }
                }
            }
        }
    }

    private fun getAutoUpFlow(): Flow<UiEvent> {
        return flow {
            while (true) {
                mtx.withLock {
                    if (!isStopped.value && isUp.value && currentUnusedUiEventsIndex < uiEvents.size) {
                        if (uiEvents[currentUnusedUiEventsIndex].isImportant()) {
                            currentState.update {
                                snapshots[currentUnusedSnapshotsIndex]
                            }
                            currentUnusedSnapshotsIndex++
                        }
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
                mtx.withLock {
                    if (!isStopped.value && !isUp.value && currentUnusedUiEventsIndex > 0) {
                        currentUnusedUiEventsIndex--
                        if (uiEvents[currentUnusedUiEventsIndex].isImportant()) {
                            currentUnusedSnapshotsIndex--
                            currentState.update {
                                snapshots[currentUnusedSnapshotsIndex]
                            }
                        }
                        emit(uiMapper reverse uiEvents[currentUnusedUiEventsIndex])
                    }
                    delay(timeBetween.value)
                }
            }
        }
    }
}
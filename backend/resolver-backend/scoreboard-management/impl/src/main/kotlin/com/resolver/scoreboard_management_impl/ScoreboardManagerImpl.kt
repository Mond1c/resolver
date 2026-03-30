package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.MutableRow
import com.resolver.resolution_logic_api.MutableScoreboard
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_api.UiEventsToScoreboardChangesMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ExperimentalCoroutinesApi
internal class ScoreboardManagerImpl<T : MutableRow>(
    private val scoreboard: MutableScoreboard<T>,
    private val uiEvents: List<UiEvent>,
    private val uiEventsToScoreboardChangesMapper: UiEventsToScoreboardChangesMapper<T>,
    private val scoreboardCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ScoreboardManager<T> {
    private val timeBetween = MutableStateFlow(ScoreboardManager.BASE_TIME_BETWEEN_MS)
    private val isUp = MutableStateFlow(true)
    private val isStopped = MutableStateFlow(true)
    private val nextSignal = MutableSharedFlow<Unit>()
    private val prevSignal = MutableSharedFlow<Unit>()
    private val scoreboardManagerScope = CoroutineScope(SupervisorJob() + scoreboardCoroutineDispatcher)
    private val mtx = Mutex()
    private var currentUnusedIndex = 0

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
        timeBetween.update { currentTimeBetween ->
            (currentTimeBetween * speedFactor).toLong()
        }
    }

    override fun next() {
        scoreboardManagerScope.launch {
            nextSignal.emit(Unit)
        }
    }

    override fun prev() {
        scoreboardManagerScope.launch {
            prevSignal.emit(Unit)
        }
    }

    override fun getUiEventsFlow(): Flow<UiEvent> {
        return flowOf(
            getNextFlow(),
            getPrevFlow(),
            getAutoUpFlow(),
            getAutoDownFlow()
        )
            .flattenMerge()
            .flowOn(scoreboardCoroutineDispatcher)

    }

    override fun getScoreboard(): MutableScoreboard<T> {
        TODO("Not yet implemented")
    }

    private fun getNextFlow(): Flow<UiEvent> {
        return flow {
            while (true) {
                mtx.withLock {
                    if (isStopped.value && currentUnusedIndex < uiEvents.size) {
                        scoreboard.applyChanges(
                            uiEventsToScoreboardChangesMapper convert uiEvents[currentUnusedIndex]
                        )
                        emit(uiEvents[currentUnusedIndex])
                        currentUnusedIndex++
                        delay(timeBetween.value)
                    }
                }
            }
        }
    }

    private fun getPrevFlow(): Flow<UiEvent> {
        return flow {
            while (true) {
                mtx.withLock {
                    if (isStopped.value && currentUnusedIndex > 0) {
                        currentUnusedIndex--
                        scoreboard.applyChanges(
                            uiEventsToScoreboardChangesMapper reverse uiEvents[currentUnusedIndex]
                        )
                        emit(uiEvents[currentUnusedIndex])
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
                    if (!isStopped.value && isUp.value && currentUnusedIndex < uiEvents.size) {
                        scoreboard.applyChanges(
                            uiEventsToScoreboardChangesMapper convert uiEvents[currentUnusedIndex]
                        )
                        emit(uiEvents[currentUnusedIndex])
                        currentUnusedIndex++
                        delay(timeBetween.value)
                    }
                }
            }
        }
    }

    private fun getAutoDownFlow(): Flow<UiEvent> {
        return flow {
            while (true) {
                mtx.withLock {
                    if (!isStopped.value && !isUp.value && currentUnusedIndex > 0) {
                        currentUnusedIndex--
                        scoreboard.applyChanges(
                            uiEventsToScoreboardChangesMapper reverse uiEvents[currentUnusedIndex]
                        )
                        emit(uiEvents[currentUnusedIndex])
                        delay(timeBetween.value)
                    }
                }
            }
        }
    }
}
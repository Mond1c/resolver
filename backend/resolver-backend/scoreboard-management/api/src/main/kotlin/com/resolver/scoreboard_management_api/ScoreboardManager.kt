package com.resolver.scoreboard_management_api

import kotlinx.coroutines.flow.Flow

abstract class ScoreboardManager(
    protected val scoreboardManagerOptions: ScoreboardManagerOptions
) {
    abstract fun start()

    abstract fun stop()

    abstract fun changeDirection()

    abstract fun applySpeedFactor(speedFactor: Double)

    abstract fun up()

    abstract fun down()

    abstract fun getUiEventsFlow(): Flow<UiEvent>

    abstract fun getScoreboard(): UiEvent.Scoreboard

    abstract fun getCountOfProblems(): Int
}
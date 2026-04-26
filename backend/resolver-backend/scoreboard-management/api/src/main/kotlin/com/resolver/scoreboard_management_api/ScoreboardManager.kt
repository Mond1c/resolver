package com.resolver.scoreboard_management_api

import kotlinx.coroutines.flow.Flow

interface ScoreboardManager {
    fun start()

    fun stop()

    fun changeDirection()

    fun applySpeedFactor(speedFactor: Double)

    fun up()

    fun down()

    fun getUiEventsFlow(): Flow<UiEvent>

    fun getScoreboard(): UiEvent.Scoreboard

    fun getCountOfProblems(): Int

    companion object {
        const val BASE_TIME_BETWEEN_MS = 1000L
    }
}
package com.resolver.scoreboard_management_api

import kotlinx.coroutines.flow.Flow
import org.icpclive.cds.api.ScoreboardRow

interface ScoreboardManager {
    fun start()

    fun stop()

    fun changeDirection()

    fun applySpeedFactor(speedFactor: Double)

    fun up()

    fun down()

    fun getUiEventsFlow(): Flow<UiEvent>

    fun getScoreboard(): List<ScoreboardRow>

    companion object {
        const val BASE_TIME_BETWEEN_MS = 1000L
    }
}
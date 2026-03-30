package com.resolver.scoreboard_management_api

import com.resolver.resolution_logic_api.MutableRow
import com.resolver.resolution_logic_api.MutableScoreboard
import kotlinx.coroutines.flow.Flow

interface ScoreboardManager<T : MutableRow> {
    fun start()

    fun stop()

    fun changeDirection()

    fun applySpeedFactor(speedFactor: Double)

    fun next()

    fun prev()

    fun getUiEventsFlow(): Flow<UiEvent>

    fun getScoreboard(): MutableScoreboard<T>
}
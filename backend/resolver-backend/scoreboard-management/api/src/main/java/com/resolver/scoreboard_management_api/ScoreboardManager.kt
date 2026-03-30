package com.resolver.scoreboard_management_api

import kotlinx.coroutines.flow.Flow

interface ScoreboardManager {
    fun start()

    fun stop()

    fun changeDirection()

    fun applySpeedFactor(speedFactor: Double)

    fun next()

    fun prev()

    fun getUiEventsFlow(): Flow<UiEvent>
}
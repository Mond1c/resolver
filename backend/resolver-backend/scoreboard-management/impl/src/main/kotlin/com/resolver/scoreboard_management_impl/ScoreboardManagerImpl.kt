package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.MutableRow
import com.resolver.resolution_logic_api.MutableScoreboard
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.UiEvent
import kotlinx.coroutines.flow.Flow

internal class ScoreboardManagerImpl<T : MutableRow>(
    private val scoreboard: MutableScoreboard<T>,
    private val uiEvents: List<UiEvent>
) : ScoreboardManager<T> {
    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun changeDirection() {
        TODO("Not yet implemented")
    }

    override fun applySpeedFactor(speedFactor: Double) {
        TODO("Not yet implemented")
    }

    override fun next() {
        TODO("Not yet implemented")
    }

    override fun prev() {
        TODO("Not yet implemented")
    }

    override fun getUiEventsFlow(): Flow<UiEvent> {
        TODO("Not yet implemented")
    }

    override fun getScoreboard(): MutableScoreboard<T> {
        TODO("Not yet implemented")
    }
}
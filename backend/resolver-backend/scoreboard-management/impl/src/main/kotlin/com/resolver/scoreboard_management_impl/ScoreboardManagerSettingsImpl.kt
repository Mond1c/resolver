package com.resolver.scoreboard_management_impl

import com.resolver.scoreboard_management_api.Direction
import com.resolver.scoreboard_management_api.ScoreboardManagerSettings
import com.resolver.scoreboard_management_api.State
import kotlinx.serialization.Serializable

@Serializable
data class ScoreboardManagerSettingsImpl(
    override val speedFactor: Double,
    override val direction: Direction,
    override val state: State,
    override val isGotoEnabled: Boolean
) : ScoreboardManagerSettings {
    companion object {
        fun provideDefault(isGotoEnabled: Boolean) = ScoreboardManagerSettingsImpl(
            speedFactor = 1.0,
            direction = Direction.UP,
            state = State.STOP,
            isGotoEnabled = isGotoEnabled
        )
    }
}
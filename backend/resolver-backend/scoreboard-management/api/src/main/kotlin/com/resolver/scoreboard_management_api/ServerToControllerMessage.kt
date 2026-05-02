package com.resolver.scoreboard_management_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.icpclive.cds.api.TeamId

@Serializable
sealed interface ServerToControllerMessage {
    @Serializable
    @SerialName("VariantsToGoto")
    class VariantsToGoto(
        val teamId: TeamId,
        val fullName: String,
        val variants: List<VariantToGoto>
    ) : ServerToControllerMessage

    @Serializable
    @SerialName("Settings")
    data class ScoreboardManagerSettings(
        val speedFactor: Double,
        val direction: Direction,
        val state: State,
        val isGotoEnabled: Boolean
    ) : ServerToControllerMessage {
        companion object {
            fun provideDefault(isGotoEnabled: Boolean) = ScoreboardManagerSettings(
                speedFactor = 1.0,
                direction = Direction.UP,
                state = State.STOP,
                isGotoEnabled = isGotoEnabled
            )
        }
    }
}

@Serializable
class VariantToGoto(
    val stateIndex: Int,
    val problemsToResolveDisplayNames: List<String>
)

@Serializable
enum class Direction {
    @SerialName("Up")
    UP,

    @SerialName("Down")
    DOWN
}

@Serializable
enum class State {
    @SerialName("Process")
    PROCESS,

    @SerialName("Stop")
    STOP
}
package com.resolver.scoreboard_management_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ScoreboardManagerSettings {
    val speedFactor: Double
    val direction: Direction
    val state: State
    val isGotoEnabled: Boolean
}

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
package com.resolver.resolver_server_api

import com.resolver.scoreboard_management_api.ScoreboardManager

abstract class Server(
    protected val scoreboardManager: ScoreboardManager
) {
    abstract suspend fun start(
        port: Int,
        host: String
    ): StartResult

    abstract suspend fun stop(): StopResult

    companion object {
        const val RESOLUTION_WS_ENDPOINT = "/resolution"
        const val RESOLUTION_CONTROL_WS_ENDPOINT = "/control"
    }
}
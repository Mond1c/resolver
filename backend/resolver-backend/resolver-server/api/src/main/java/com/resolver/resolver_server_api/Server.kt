package com.resolver.resolver_server_api

import com.resolver.scoreboard_management_api.ScoreboardManager

abstract class Server(
    protected val scoreboardManager: ScoreboardManager,
    protected val serverOptions: ServerOptions
) {
    abstract suspend fun start(
        startServerOptions: StartServerOptions
    ): StartResult

    abstract suspend fun stop(): StopResult
}
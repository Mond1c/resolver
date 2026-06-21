package com.resolver.resolver_server_api

import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.util_api.ResolverAccounts

abstract class Server(
    protected val scoreboardManager: ScoreboardManager,
    protected val serverOptions: ServerOptions,
    protected val resolverAccounts: ResolverAccounts
) {
    abstract suspend fun start(
        startServerOptions: StartServerOptions
    ): StartResult

    abstract suspend fun stop(): StopResult
}
package com.resolver.resolver_server_di

import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.ServerOptions
import com.resolver.resolver_server_impl.ServerImpl
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.util_api.ResolverAccounts
import kotlinx.serialization.json.Json

object ResolverServerComponent {
    fun provideServer(
        scoreboardManager: ScoreboardManager,
        json: Json,
        resolverAccounts: ResolverAccounts,
        serverOptions: ServerOptions
    ): Server {
        return ServerImpl(
            json = json,
            scoreboardManager = scoreboardManager,
            serverOptions = serverOptions,
            resolverAccounts = resolverAccounts
        )
    }
}
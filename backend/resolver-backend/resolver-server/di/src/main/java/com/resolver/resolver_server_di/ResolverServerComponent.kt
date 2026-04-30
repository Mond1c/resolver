package com.resolver.resolver_server_di

import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.ServerOptions
import com.resolver.resolver_server_impl.ServerImpl
import com.resolver.scoreboard_management_api.ScoreboardManager
import kotlinx.serialization.json.Json

object ResolverServerComponent {
    fun provideServer1(
        scoreboardManager: ScoreboardManager,
        json: Json,
        serverOptions: ServerOptions
    ): Server {
        return ServerImpl(
            json = json,
            scoreboardManager = scoreboardManager,
            serverOptions = serverOptions
        )
    }
}
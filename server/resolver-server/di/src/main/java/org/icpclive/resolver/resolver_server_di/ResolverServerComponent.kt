package org.icpclive.resolver.resolver_server_di

import kotlinx.serialization.json.Json
import org.icpclive.resolver.resolver_server_api.Server
import org.icpclive.resolver.resolver_server_api.ServerOptions
import org.icpclive.resolver.resolver_server_impl.ServerImpl
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.util_api.ResolverAccounts

object ResolverServerComponent {
    fun provideServer(
        scoreboardManager: ScoreboardManager,
        json: Json,
        resolverAccounts: ResolverAccounts,
        serverOptions: ServerOptions,
    ): Server {
        return ServerImpl(
            json = json,
            scoreboardManager = scoreboardManager,
            serverOptions = serverOptions,
            resolverAccounts = resolverAccounts
        )
    }
}
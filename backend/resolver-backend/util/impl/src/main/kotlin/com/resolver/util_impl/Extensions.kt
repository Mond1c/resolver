package com.resolver.util_impl

import com.resolver.resolver_server_api.ServerOptions
import com.resolver.resolver_server_api.StartServerOptions
import com.resolver.scoreboard_management_api.ScoreboardManagerOptions
import com.resolver.util_api.ResolverOptions

internal fun ResolverOptions.extractServerOptions(): ServerOptions = ServerOptions(
    resolutionControlWsEndpoint = resolutionControlWsEndpoint,
    resolutionWsEndpoint = resolutionWsEndpoint
)

internal fun ResolverOptions.extractScoreboardManagerOptions(): ScoreboardManagerOptions = ScoreboardManagerOptions(
    baseTimeBetweenMs = baseTimeBetweenMs,
    isGotoEnabled = isGotoEnabled
)

internal fun ResolverOptions.extractStartServerOptions(): StartServerOptions = StartServerOptions(
    host = host,
    port = port
)
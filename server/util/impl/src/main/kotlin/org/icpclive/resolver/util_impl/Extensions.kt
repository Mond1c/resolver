package org.icpclive.resolver.util_impl

import org.icpclive.resolver.resolver_server_api.ServerOptions
import org.icpclive.resolver.resolver_server_api.StartServerOptions
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManagerOptions
import org.icpclive.resolver.util_api.ResolverOptions

internal fun ResolverOptions.extractServerOptions(): ServerOptions = ServerOptions(
    resolutionControlWsEndpoint = resolutionControlWsEndpoint,
    resolutionWsEndpoint = resolutionWsEndpoint,
    replay = replay
)

internal fun ResolverOptions.extractScoreboardManagerOptions(): ScoreboardManagerOptions =
    ScoreboardManagerOptions(
        baseTimeBetweenMs = baseTimeBetweenMs,
        isGotoEnabled = isGotoEnabled,
        replay = replay
    )

internal fun ResolverOptions.extractStartServerOptions(): StartServerOptions = StartServerOptions(
    host = host,
    port = port
)

internal fun String.resolveCredential(creds: Map<String, String>): String {
    val prefix = $$"$creds."
    if (startsWith(prefix)) {
        val name = substring(prefix.length)
        val cred = creds[name]
        if (cred != null) {
            return cred
        }
    }
    return this
}

internal fun ResolverAccount.resolveCredentials(creds: Map<String, String>): ResolverAccount {
    return ResolverAccount(
        login = login.resolveCredential(creds),
        password = password.resolveCredential(creds)
    )
}

internal fun List<ResolverAccount>.resolveCredentials(creds: Map<String, String>): List<ResolverAccount> {
    return map { it.resolveCredentials(creds) }
}
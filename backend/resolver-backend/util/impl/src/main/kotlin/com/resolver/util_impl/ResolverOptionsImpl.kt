package com.resolver.util_impl

import com.resolver.util_api.ResolverCommandLineOptions
import com.resolver.util_api.ResolverOptions
import kotlinx.serialization.Serializable

@Serializable
data class ResolverOptionsImpl(
    override val baseTimeBetweenMs: Long,
    override val resolutionControlWsEndpoint: String,
    override val resolutionWsEndpoint: String,
    override val host: String,
    override val port: Int
) : ResolverOptions {
    override fun mergeWithCommandLineOptions(commandLineOptions: ResolverCommandLineOptions): ResolverOptions {
        return ResolverOptionsImpl(
            baseTimeBetweenMs = baseTimeBetweenMs,
            resolutionControlWsEndpoint = resolutionControlWsEndpoint,
            resolutionWsEndpoint = resolutionWsEndpoint,
            host = commandLineOptions.host ?: host,
            port = commandLineOptions.port ?: port
        )
    }

    companion object {
        val DEFAULT = ResolverOptionsImpl(
            baseTimeBetweenMs = 1000,
            resolutionControlWsEndpoint = "/control",
            resolutionWsEndpoint = "/resolution",
            host = "0.0.0.0",
            port = 8080
        )
    }
}
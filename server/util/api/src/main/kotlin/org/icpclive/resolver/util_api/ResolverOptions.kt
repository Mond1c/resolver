package org.icpclive.resolver.util_api

interface ResolverOptions {
    val baseTimeBetweenMs: Long
    val resolutionControlWsEndpoint: String
    val resolutionWsEndpoint: String
    val host: String
    val port: Int
    val isGotoEnabled: Boolean
    val replay: Int

    fun mergeWithCommandLineOptions(commandLineOptions: ResolverCommandLineOptions): ResolverOptions
}
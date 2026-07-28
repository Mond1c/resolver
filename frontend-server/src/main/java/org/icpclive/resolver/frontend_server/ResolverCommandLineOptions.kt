package org.icpclive.resolver.frontend_server

import com.github.ajalt.clikt.parameters.options.default

internal class ResolverCommandLineOptions : BaseCommandLineOptions() {
    override val endpoint by endpointOption
        .default("resolver")
}
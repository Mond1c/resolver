package org.icpclive.resolver.frontend_server

import com.github.ajalt.clikt.parameters.options.default

internal class ResolverControlCommandLineOptions : BaseCommandLineOptions() {
    override val endpoint by endpointOption
        .default("resolver-control")
}
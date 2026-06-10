package com.resolver.main.control

import com.github.ajalt.clikt.parameters.options.default
import com.resolver.main.base.BaseCommandLineOptions
import com.resolver.main.base.endpointOption

class ResolverControlCommandLineOptions : BaseCommandLineOptions() {
    override val endpoint by endpointOption
        .default("resolver-control")
}
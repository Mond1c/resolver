package com.resolver.main.resolver

import com.github.ajalt.clikt.parameters.options.default
import com.resolver.main.base.BaseCommandLineOptions
import com.resolver.main.base.endpointOption

class ResolverCommandLineOptions : BaseCommandLineOptions() {
    override val endpoint by endpointOption
        .default("resolver")
}
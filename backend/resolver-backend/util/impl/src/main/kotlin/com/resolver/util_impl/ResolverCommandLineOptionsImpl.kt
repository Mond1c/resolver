package com.resolver.util_impl

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.resolver.util_api.ResolverCommandLineOptions

open class ResolverCommandLineOptionsImpl : ResolverCommandLineOptions() {
    override val port by option(
        "-p", "--port",
        help = "Port to connect to"
    )
        .int()

    override val host by option(
        "-h", "--host",
        help = "Host to connect to"
    )

    override val genAwards by option(
        "--gen-awards",
        help = "Write file in config directory where time of award appearance can be redetermined. " +
                "If true, then file is written and program exits. " +
                "If false: each award appears after each fully resolved team got it, if corresponding file does not " +
                "exist in config directory, else file awards_behaviour.json is used."
    )
        .flag(default = false)

    override val genResolverOptions by option(
        "--gen-resolver-options",
        help = "Write file in config directory where resolver option can be redetermined."
    )
        .flag(default = false)
}
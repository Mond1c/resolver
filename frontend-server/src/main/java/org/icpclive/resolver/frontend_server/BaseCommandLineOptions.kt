package org.icpclive.resolver.frontend_server

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int

internal abstract class BaseCommandLineOptions : OptionGroup() {
    val port by option(
        "-p", "--port",
        help = "Port to connect to"
    )
        .int()
        .default(8080)

    val host by option(
        "-h", "--host",
        help = "Host to connect to"
    )
        .default("0.0.0.0")

    abstract val endpoint: String
}
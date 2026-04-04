package com.resolver

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import org.icpclive.cds.cli.CdsCommandLineOptions

open class ResolverCommandLineOptions : CdsCommandLineOptions() {
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
}
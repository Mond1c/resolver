package com.resolver.util_api

import org.icpclive.cds.cli.CdsCommandLineOptions

abstract class ResolverCommandLineOptions : CdsCommandLineOptions() {
    abstract val port: Int
    abstract val host: String
    abstract val genAwards: Boolean
}
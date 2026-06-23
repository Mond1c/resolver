package org.icpclive.resolver.util_api

import org.icpclive.cds.cli.CdsCommandLineOptions

abstract class ResolverCommandLineOptions : CdsCommandLineOptions() {
    abstract val port: Int?
    abstract val host: String?
    abstract val genAwards: Boolean
    abstract val genResolverOptions: Boolean
    abstract val enableGoto: Boolean
}
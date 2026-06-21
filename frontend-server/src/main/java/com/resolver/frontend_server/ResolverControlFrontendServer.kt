package com.resolver.frontend_server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate

object ResolverControlFrontendServer : CliktCommand() {
    internal val cmdOptions by ResolverControlCommandLineOptions()

    override fun run() {
        startServer(cmdOptions)
    }
}
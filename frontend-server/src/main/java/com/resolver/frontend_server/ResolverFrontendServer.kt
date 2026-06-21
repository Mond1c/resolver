package com.resolver.frontend_server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate

object ResolverFrontendServer : CliktCommand() {
    internal val cmdOptions by ResolverCommandLineOptions()

    override fun run() {
        startServer(cmdOptions)
    }
}
package com.resolver.main.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.main.base.startServer

object ResolverFrontendServer : CliktCommand() {
    val cmdOptions by ResolverCommandLineOptions()

    override fun run() {
        startServer(cmdOptions)
    }
}

fun main(args: Array<String>) = ResolverFrontendServer.main(args)
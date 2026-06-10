package com.resolver.main.control

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.main.base.startServer

object ResolverControlFrontendServer : CliktCommand() {
    val cmdOptions by ResolverControlCommandLineOptions()

    override fun run() {
        startServer(cmdOptions)
    }
}

fun main(args: Array<String>) = ResolverControlFrontendServer.main(args)
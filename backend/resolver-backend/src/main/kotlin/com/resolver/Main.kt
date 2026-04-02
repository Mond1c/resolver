package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import kotlinx.coroutines.runBlocking
import org.icpclive.cds.CommentaryMessagesUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ContestStatus

class App : CliktCommand() {
    private val resolverOptions by ResolverCommandLineOptions()

    override fun run() {
        runBlocking {
            val states = mutableListOf<ContestState>()
            resolverOptions.toFlow().addComputedData {
                firstToSolves = true
                submissionResultsAfterFreeze = true
                autoFinalize = true
            }.contestState().collect { state ->
                when (val lastEvent = state.lastEvent) {
                    is CommentaryMessagesUpdate -> {}
                    is InfoUpdate -> {
                        if (lastEvent.newInfo.status is ContestStatus.FINALIZED) {
                            states.add(state)
                            ResolutionLogicComponent.greedyICPCResolver.resolve(states)
                                .steps
                                .joinToString(separator = "\n")
                                .also(::println)
                        }
                    }

                    is RunUpdate -> {
                        states.add(state)
                    }
                }
            }
        }
    }
}

fun main(args: Array<String>) = App().main(args)
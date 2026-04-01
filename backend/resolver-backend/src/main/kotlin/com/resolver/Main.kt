package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.cli.CdsCommandLineOptions

class App : CliktCommand() {
    private val cdsOptions by CdsCommandLineOptions()

    override fun run() {
        val scope = CoroutineScope(Dispatchers.Default)
        val mtx = Mutex()
        val states = mutableListOf<ContestState>()
        scope.launch {
            cdsOptions
                .toFlow()
                .addComputedData {
                    firstToSolves = true
                    submissionResultsAfterFreeze = false
                }
                .contestState()
                .collect {
                    mtx.withLock {
                        states.add(it)
                    }
                }
        }
        Thread.sleep(5000)
        val contestState = states.last()
        println(contestState.infoAfterEvent?.status)
    }
}

fun main(args: Array<String>) = App().main(args)
package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.scoreboard_management_di.ScoreboardManagementComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
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
        val removed = mutableListOf<ContestState>()
        val notRemoved = mutableListOf<ContestState>()
        val semaphore = Semaphore(2, 2)
        runBlocking {
            launch {
                resolverOptions.toFlow().addComputedData {
                    firstToSolves = true
                    submissionResultsAfterFreeze = false
                    autoFinalize = true
                }
                    .contestState()
                    .collect { state ->
                        when (val lastEvent = state.lastEvent) {
                            is CommentaryMessagesUpdate -> {}
                            is InfoUpdate -> {
                                if (lastEvent.newInfo.status is ContestStatus.OVER) {
                                    removed.add(state)
                                    semaphore.release()
                                }
                            }

                            is RunUpdate -> {
                                removed.add(state)
                            }
                        }
                    }
            }

            launch {
                resolverOptions.toFlow().addComputedData {
                    firstToSolves = true
                    submissionResultsAfterFreeze = true
                    autoFinalize = true
                }
                    .contestState()
                    .collect { state ->
                        when (val lastEvent = state.lastEvent) {
                            is CommentaryMessagesUpdate -> {}
                            is InfoUpdate -> {
                                if (lastEvent.newInfo.status is ContestStatus.OVER) {
                                    notRemoved.add(state)
                                    semaphore.release()
                                }
                            }

                            is RunUpdate -> {
                                notRemoved.add(state)
                            }
                        }
                    }
            }

            semaphore.acquire()
            semaphore.acquire()

            val frozenState = removed.last()
            val resolver = ResolutionLogicComponent.greedyICPCResolver
            val result = resolver.resolve(notRemoved)
            val manager = ScoreboardManagementComponent.provideScoreboardManager1(
                frozenState = frozenState,
                snapshots = result.snapshots,
                steps = result.steps
            )
            launch {
                manager.getUiEventsFlow().collect {
                    println(it)
                }
            }
            launch {
                delay(1000)
                launch {
                    manager.start()
                }
                delay(15000)
                manager.changeDirection()
            }
        }
    }
}

fun main(args: Array<String>) = App().main(args)
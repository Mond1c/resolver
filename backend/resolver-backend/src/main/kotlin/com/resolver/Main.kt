package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.util_di.ResolverUtilComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.icpclive.cds.CommentaryMessagesUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.applyEvent
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ContestStatus
import org.icpclive.cds.api.currentContestTime
import org.icpclive.cds.api.toTeamId

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

            val calc = ResolverUtilComponent.scoreboardCalculator1
            var remLastState = removed.last()
            val score1 = calc.calculateScoreboard(remLastState.infoAfterEvent, remLastState.runsAfterEvent)
            score1?.rows["spb512".toTeamId()]?.problemResults?.also(::println)
            val frozen =
                notRemoved.filter { it.infoAfterEvent!!.currentContestTime > remLastState.infoAfterEvent!!.freezeTime!! }
                    .filter { it.lastEvent is RunUpdate }
                    .filter { (it.lastEvent as RunUpdate).newInfo.teamId == "spb512".toTeamId() }
            for (run in frozen) {
                remLastState = remLastState.applyEvent(run.lastEvent)
            }
            val score2 = calc.calculateScoreboard(
                remLastState.infoAfterEvent,
                remLastState.runsAfterEvent
            )
            score2?.rows["spb512".toTeamId()]?.problemResults?.also(::println)
        }
    }
}

fun main(args: Array<String>) = App().main(args)
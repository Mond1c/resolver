package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.icpclive.cds.CommentaryMessagesUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.applyEvent
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.*
import org.icpclive.cds.scoreboard.getScoreboardCalculator

class App : CliktCommand() {
    private val resolverOptions by ResolverCommandLineOptions()

    override fun run() {
        runBlocking {
            val states = mutableListOf<ContestState>()
            launch {
                resolverOptions.toFlow().addComputedData {
                    firstToSolves = true
                    submissionResultsAfterFreeze = true
                }
                    .contestState()
                    .collect { state ->
                        when (val lastEvent = state.lastEvent) {
                            is CommentaryMessagesUpdate -> {}
                            is InfoUpdate -> {
                                if (lastEvent.newInfo.status is ContestStatus.OVER) {
                                    states.add(state)
                                    f(states)
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
}

fun f(states: List<ContestState>) {
    val endContestState = states.findLast { it.lastEvent is InfoUpdate }!!
    val runs = states.filter { it.lastEvent is RunUpdate }
    val notFrozenTemp = runs.filter {
        (it.lastEvent as RunUpdate).newInfo.time < it.infoAfterEvent?.freezeTime!!
    }
    val notFrozenTemp1 = notFrozenTemp
        .groupBy {
            (it.lastEvent as RunUpdate).newInfo.teamId
        }
    val notFrozen =
        notFrozenTemp1
            .mapValues { state ->
                state.value.groupBy {
                    (it.lastEvent as RunUpdate).newInfo.problemId
                }
            }
    val frozenTemp = runs.filter {
        (it.lastEvent as RunUpdate).newInfo.time >= it.infoAfterEvent?.freezeTime!!
    }
    val frozen =
        frozenTemp
            .groupBy {
                (it.lastEvent as RunUpdate).newInfo.teamId
            }
            .mapValues { state ->
                state.value.groupBy {
                    (it.lastEvent as RunUpdate).newInfo.problemId
                }
            }
    val currentContestState = notFrozenTemp.last()
    val contestInfo = currentContestState.infoBeforeEvent!!
    val calculator = getScoreboardCalculator(contestInfo, OptimismLevel.NORMAL)
    val rows = notFrozen.mapValues {
        calculator.getScoreboardRow(
            contestInfo,
            notFrozen[it.key]!!.flatMap { it.value.map { (it.lastEvent as RunUpdate).newInfo } }
        )
    }
    val ranks = calculator.getRanking(contestInfo, rows)
    println(ranks.order)
    val x =
        frozenTemp.find {
            (it.lastEvent as RunUpdate).newInfo.teamId.value == "spb428" &&
                    ((it.lastEvent as RunUpdate).newInfo.result as RunResult.ICPC).verdict == Verdict.Accepted &&
                    (it.lastEvent as RunUpdate).newInfo.problemId.value == "J"
        }
    val calculator1 = getScoreboardCalculator(x?.infoAfterEvent!!, OptimismLevel.NORMAL)
    val rows1 = x.runsAfterEvent.values.groupBy { it.teamId }
        .mapValues {
            calculator1.getScoreboardRow(
                x.infoAfterEvent!!,
                it.value
            )
        }
    val ranks1 = calculator1.getRanking(contestInfo, rows1)
    println(ranks1.order)
    println(ranks1.order.size)
//    println(notFrozen.entries.joinToString("\n"))
//    println(frozen.map { it.lastEvent })
    val newContestState = currentContestState.applyEvent(x.lastEvent)
    val contestInfo2 = newContestState.infoAfterEvent!!
    val calculator2 = getScoreboardCalculator(contestInfo2, OptimismLevel.NORMAL)
    val rows2 = newContestState.runsAfterEvent.values.groupBy { it.teamId }.mapValues {
        calculator2.getScoreboardRow(
            contestInfo2,
            it.value
        )
    }
    val ranks2 = calculator.getRanking(contestInfo2, rows2)
    println(ranks2.order)
}

fun main(args: Array<String>) = App().main(args)
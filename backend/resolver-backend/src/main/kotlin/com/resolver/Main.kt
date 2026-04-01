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
import org.icpclive.cds.scoreboard.Ranking
import org.icpclive.cds.scoreboard.getScoreboardCalculator
import kotlin.time.Duration

class App : CliktCommand() {
    private val resolverOptions by ResolverCommandLineOptions()

    override fun run() {
        runBlocking {
            val states = mutableListOf<ContestState>()
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
                                if (lastEvent.newInfo.status is ContestStatus.FINALIZED) {
                                    states.add(state)
                                    resolveICPC(states).steps.joinToString(separator = "\n").also(::println)
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

sealed interface ResolutionStep {
    data class RejectResolutionStep(
        val teamId: TeamId,
        val problem: ProblemId,
    ) : ResolutionStep

    data class ICPCAcceptResolutionStep(
        val teamId: TeamId,
        val problemId: ProblemId,
        val oldRank: Int,
        val newRank: Int,
        val oldIndex: Int,
        val newIndex: Int,
        val isFirstToSolve: Boolean,
        val wrongAttempts: Int,
        val newTotalPenalty: Duration
    ) : ResolutionStep
}

data class ResolutionResult(
    val steps: List<ResolutionStep>,
)

data class Calculations(
    val rows: Map<TeamId, ScoreboardRow>,
    val ranks: Ranking
)

fun calculate(contestState: ContestState): Calculations {
    val contestInfo = contestState.infoAfterEvent ?: TODO("infoAfterEvent is null")
    val calculator = getScoreboardCalculator(contestInfo, OptimismLevel.NORMAL)
    val rows = contestState.runsAfterEvent.values
        .groupBy { runInfo ->
            runInfo.teamId
        }
        .mapValues { runInfoEntries ->
            calculator.getScoreboardRow(contestInfo, runInfoEntries.value)
        }
    val ranks = calculator.getRanking(contestInfo, rows)
    return Calculations(rows, ranks)
}

fun resolveICPC(states: List<ContestState>): ResolutionResult {
    val teamsCount = states.last().infoAfterEvent?.teams?.size ?: TODO("infoAfterEvent is null")
    val runs = states.filter { it.lastEvent is RunUpdate }
    val notFrozenContestStates = runs.filter {
        (it.lastEvent as RunUpdate).newInfo.time < (it.infoAfterEvent?.freezeTime
            ?: TODO("infoAfterEvent or freezeTime is null"))
    }
    val contestStateRightBeforeFreeze = notFrozenContestStates.last()
    val teamIdToProblemIdToFrozenContestStates = runs
        .filter {
            (it.lastEvent as RunUpdate).newInfo.time >= (it.infoAfterEvent?.freezeTime
                ?: TODO("infoAfterEvent or freezeTime is null"))
        }
        .groupBy { state ->
            (state.lastEvent as RunUpdate).newInfo.teamId
        }
        .mapValues { contestStates ->
            contestStates.value
                .groupBy { state ->
                    (state.lastEvent as RunUpdate).newInfo.problemId
                }
        }
    var currentContestState = contestStateRightBeforeFreeze
    val steps = mutableListOf<ResolutionStep>()
    var currentUnresolvedIndex = teamsCount - 1
    val problemIdToIndex = currentContestState.infoAfterEvent?.scoreboardProblems?.associate { it.id to it.ordinal }
        ?: TODO("infoAfterEvent is null")
    while (currentUnresolvedIndex >= 0) {
        val (rows, ranks) = calculate(currentContestState)
        val teamId = ranks.order[currentUnresolvedIndex]
        val problemIdToFrozenContestStates = teamIdToProblemIdToFrozenContestStates[teamId]
        if (problemIdToFrozenContestStates == null) {
            currentUnresolvedIndex--
            continue
        }
        var runToChoose: ContestState? = null
        val scoreboardRowBeforeResolution = rows[ranks.order[currentUnresolvedIndex]] ?: TODO("Unexpected null")
        for (entry in problemIdToFrozenContestStates) {
            problemLoop@ for (frozenContestState in entry.value) {
                val runInfo = (frozenContestState.lastEvent as RunUpdate).newInfo
                val icpcResult = runInfo.result as RunResult.ICPC
//                if (!icpcResult.verdict.isAccepted) {
//                    runToChoose = frozenContestState
//                    break@problemLoop
//                } else
                if (icpcResult.verdict.isAccepted &&
                    !(scoreboardRowBeforeResolution.problemResults[problemIdToIndex[runInfo.problemId]
                        ?: TODO("Unexpected null")] as ICPCProblemResult).isSolved
                ) {
                    runToChoose = frozenContestState
                    break@problemLoop
                }
            }
        }
        val oldIndex = currentUnresolvedIndex
        val oldRank = ranks.ranks[oldIndex]
        if (runToChoose == null) {
            currentUnresolvedIndex--
        } else {
            println(currentContestState.lastEvent as RunUpdate)
            currentContestState = currentContestState.applyEvent(runToChoose.lastEvent)
            val (rows, ranks) = calculate(currentContestState)
            var newIndex = 0
            for (i in 0..<teamsCount) {
                if (ranks.order[i] == teamId) {
                    newIndex = i
                    break
                }
            }
            val newRank = ranks.ranks[newIndex]
            val scoreboardRowAfterResolution = rows[ranks.order[newIndex]] ?: TODO("Unexpected null")
            val runInfo = (currentContestState.lastEvent as RunUpdate).newInfo
            val icpcResult = runInfo.result as RunResult.ICPC
            if (!icpcResult.verdict.isAccepted) {
                steps.add(ResolutionStep.RejectResolutionStep(runInfo.teamId, runInfo.problemId))
            } else {
                val problemResult = scoreboardRowAfterResolution.problemResults[problemIdToIndex[runInfo.problemId]
                    ?: TODO("Unexpected null")] as ICPCProblemResult
                steps.add(
                    ResolutionStep.ICPCAcceptResolutionStep(
                        teamId = runInfo.teamId,
                        problemId = runInfo.problemId,
                        oldRank = oldRank,
                        newRank = newRank,
                        oldIndex = oldIndex,
                        newIndex = newIndex,
                        isFirstToSolve = problemResult.isSolved,
                        wrongAttempts = problemResult.wrongAttempts,
                        newTotalPenalty = scoreboardRowAfterResolution.penalty
                    )
                )
            }
        }
    }
    return ResolutionResult(
        steps = steps
    )
}

fun main(args: Array<String>) = App().main(args)
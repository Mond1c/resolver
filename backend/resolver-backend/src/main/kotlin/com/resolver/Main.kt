package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import kotlinx.collections.immutable.PersistentMap
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
                }.contestState().collect { state ->
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
        val problemId: ProblemId,
        val wrongAttempts: Int
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
    return calculate(contestState.infoAfterEvent, contestState.runsAfterEvent)
}

fun calculate(contestInfo: ContestInfo?, runs: PersistentMap<RunId, RunInfo>): Calculations {
    val contestInfo = contestInfo ?: TODO("contestInfo is null")
    val calculator = getScoreboardCalculator(contestInfo, OptimismLevel.NORMAL)
    val rows = runs.values.groupBy { runInfo ->
        runInfo.teamId
    }.mapValues { runInfoEntries ->
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
    val teamIdToProblemIdToFrozenContestStates: HashMap<TeamId, HashMap<ProblemId, List<ContestState>>> =
        HashMap(runs.filter {
            (it.lastEvent as RunUpdate).newInfo.time >= (it.infoAfterEvent?.freezeTime
                ?: TODO("infoAfterEvent or freezeTime is null"))
        }.groupBy { state ->
            (state.lastEvent as RunUpdate).newInfo.teamId
        }.mapValues { contestStates ->
            HashMap(contestStates.value.groupBy { state ->
                (state.lastEvent as RunUpdate).newInfo.problemId
            })
        })
    var currentContestState = contestStateRightBeforeFreeze
    val steps = mutableListOf<ResolutionStep>()
    var currentUnresolvedIndex = teamsCount - 1
    val problemIdToIndex = currentContestState.infoAfterEvent?.scoreboardProblems?.associate { it.id to it.ordinal }
        ?: TODO("infoAfterEvent is null")
    while (currentUnresolvedIndex >= 0) {
        val (rows, ranking) = calculate(currentContestState)
        val teamId = ranking.order[currentUnresolvedIndex]
        val problemIdToFrozenContestStates = teamIdToProblemIdToFrozenContestStates[teamId]
        if (problemIdToFrozenContestStates == null) {
            currentUnresolvedIndex--
            continue
        }
        var contestStatesToChoose: List<ContestState>? = null
        val scoreboardRowBeforeResolution = rows[ranking.order[currentUnresolvedIndex]] ?: TODO("Unexpected null")
        val oldIndex = currentUnresolvedIndex
        val oldRank = ranking.ranks[oldIndex]
        val oldPenalty = scoreboardRowBeforeResolution.penalty
        var currentPenaltyDelta = Duration.INFINITE
        var currentMaxSolved = scoreboardRowBeforeResolution.totalScore
        var isSolvedFound = false
        for (entry in problemIdToFrozenContestStates) {
            var newRuns = currentContestState.runsAfterEvent
            for (frozenContestState in entry.value) {
                val runInfo = (frozenContestState.lastEvent as RunUpdate).newInfo
                newRuns = newRuns.put(runInfo.id, runInfo)
            }
            val (rows, ranking) = calculate(currentContestState.infoAfterEvent, newRuns)
            val newIndex = ranking.order.indexOf(teamId)
            val scoreboardRowAfterTestResolution = rows[ranking.order[newIndex]] ?: TODO("Unexpected null")
            val testPenalty = scoreboardRowAfterTestResolution.penalty
            val testSolved = scoreboardRowAfterTestResolution.totalScore
            if (testSolved > currentMaxSolved) {
                currentMaxSolved = testSolved
                isSolvedFound = true
                contestStatesToChoose = entry.value
            } else if (isSolvedFound && testPenalty != oldPenalty && testPenalty - oldPenalty < currentPenaltyDelta) {
                currentPenaltyDelta = testPenalty - oldPenalty
                contestStatesToChoose = entry.value
            } else if (!isSolvedFound) {
                contestStatesToChoose = entry.value
            }
        }
        if (contestStatesToChoose != null) {
            val runInfo =
                ((contestStatesToChoose.lastOrNull() ?: TODO("Unexpected null")).lastEvent as RunUpdate).newInfo
            (teamIdToProblemIdToFrozenContestStates[runInfo.teamId]
                ?: TODO("Unexpected null")).remove(runInfo.problemId)
        }
        if (contestStatesToChoose == null) {
            currentUnresolvedIndex--
        } else {
            contestStatesToChoose.forEach { contestState ->
                currentContestState = currentContestState.applyEvent(contestState.lastEvent)
            }
            val (rows, ranking) = calculate(currentContestState)
            val newIndex = ranking.order.indexOf(teamId)
            val newRank = ranking.ranks[newIndex]
            val scoreboardRowAfterResolution = rows[ranking.order[newIndex]] ?: TODO("Unexpected null")
            val runInfo = (currentContestState.lastEvent as RunUpdate).newInfo
            val icpcResult = runInfo.result as RunResult.ICPC
            val problemResult = scoreboardRowAfterResolution.problemResults[problemIdToIndex[runInfo.problemId]
                ?: TODO("Unexpected null")] as ICPCProblemResult
            if (!icpcResult.verdict.isAccepted) {
                steps.add(
                    ResolutionStep.RejectResolutionStep(
                        runInfo.teamId,
                        runInfo.problemId,
                        problemResult.wrongAttempts
                    )
                )
            } else {
                steps.add(
                    ResolutionStep.ICPCAcceptResolutionStep(
                        teamId = runInfo.teamId,
                        problemId = runInfo.problemId,
                        oldRank = oldRank,
                        newRank = newRank,
                        oldIndex = oldIndex,
                        newIndex = newIndex,
                        isFirstToSolve = problemResult.isFirstToSolve,
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
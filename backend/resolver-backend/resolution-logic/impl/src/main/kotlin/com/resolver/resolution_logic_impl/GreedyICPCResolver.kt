package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.ResolutionResult
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.util_api.ScoreboardCalculator
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.applyEvent
import org.icpclive.cds.api.*
import kotlin.time.Duration

class GreedyICPCResolver(
    private val scoreboardCalculator: ScoreboardCalculator
) : Resolver {
    override fun resolve(states: List<ContestState>): ResolutionResult {
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
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                ?: TODO("Unexpected null")
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
                val (rows, ranking) = scoreboardCalculator.calculateScoreboard(
                    currentContestState.infoAfterEvent,
                    newRuns
                ) ?: TODO("Unexpected null")
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
                val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                    ?: TODO("Unexpected null")
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
        return ResolutionResultImpl(
            steps = steps,
            contestStateRightBeforeFreeze = contestStateRightBeforeFreeze
        )
    }
}
package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.*
import com.resolver.util_api.ScoreboardCalculator
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.*
import kotlin.time.Duration

@Suppress("DuplicatedCode")
class GreedyIOIResolver(
    private val scoreboardCalculator: ScoreboardCalculator,
    private val awardsHandler: AwardsHandler
) : Resolver {
    private class CellAnalyzeResult(
        val contestStatesToApply: List<ContestState>? = null,
        val currentIsBetterFoundResult: Boolean,
        val currentPenaltyDeltaResult: Duration,
        val currentTotalScoreResult: Double
    )

    private class DecisionPreparationResult(
        val step: ResolutionStep?,
        val currentContestStateResult: ContestState,
        val problemIdToResolve: ProblemId?,
        val currentUnresolvedIndexResult: Int
    )

    override fun resolve(
        frozenState: ContestState,
        states: List<ContestState>,
        awardIdToAwardBehaviour: Map<String, AwardBehaviour>
    ): ResolutionResult {
        val awardIdToTeamIds = scoreboardCalculator.getAwardIdToTeamIds(states) ?: TODO("Unexpected null")
        val teamsCount =
            states.lastOrNull()?.infoAfterEvent?.teams?.size ?: TODO("states is empty or infoAfterEvent is null")
        val runs = states.getRunUpdates()
        val teamIdToProblemIdToFrozenContestStates = runs.getProblemIdToTeamIdToFrozenContestStates()
        var currentContestState = frozenState
        val steps = mutableListOf<ResolutionStep>()
        var currentUnresolvedIndex = teamsCount - 1
        val snapshots = mutableListOf<ContestState>()
        val teamsWhoHaveAtLeastOneResolvedProblem = hashSetOf<TeamId>()
        while (currentUnresolvedIndex >= 0) {
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                ?: TODO("Unexpected null")
            val teamId = ranking.order[currentUnresolvedIndex]
            val problemIdToFrozenContestStates = teamIdToProblemIdToFrozenContestStates[teamId]
            if (problemIdToFrozenContestStates == null) {
                if (!teamsWhoHaveAtLeastOneResolvedProblem.contains(teamId)) {
                    steps.add(
                        ResolutionStep.WithTeamId.NoResolvedProblemsForTeam(
                            teamId = teamId,
                            index = currentUnresolvedIndex
                        )
                    )
                }
                awardsHandler.handleAwards(
                    steps = steps,
                    awards = ranking.awards,
                    awardIdToAwardBehaviour = awardIdToAwardBehaviour,
                    awardIdToTeamIds = awardIdToTeamIds,
                    teamId = teamId
                )
                currentUnresolvedIndex--
                continue
            }
            teamsWhoHaveAtLeastOneResolvedProblem.add(teamId)
            var contestStatesToApply: List<ContestState>? = null
            val scoreboardRowBeforeResolution = rows[ranking.order[currentUnresolvedIndex]] ?: TODO("Unexpected null")
            val oldIndex = currentUnresolvedIndex
            val oldPenalty = scoreboardRowBeforeResolution.penalty
            var currentPenaltyDelta = Duration.INFINITE
            var currentIsBetterFound = false
            var currentTotalScore = scoreboardRowBeforeResolution.totalScore
            for (entry in problemIdToFrozenContestStates) {
                analyzePendingCell(
                    currentContestState = currentContestState,
                    frozenContestStates = entry.value,
                    teamId = teamId,
                    currentPenaltyDelta = currentPenaltyDelta,
                    currentTotalScore = currentTotalScore,
                    oldPenalty = oldPenalty,
                    currentIsBetterFound = currentIsBetterFound,
                ).apply {
                    currentIsBetterFound = currentIsBetterFoundResult
                    currentTotalScore = currentTotalScoreResult
                    currentPenaltyDelta = currentPenaltyDeltaResult
                    this.contestStatesToApply?.let { result ->
                        contestStatesToApply = result
                    }
                }
            }
            prepareDecision(
                currentContestState = currentContestState,
                contestStatesToApply = contestStatesToApply,
                scoreboardRowBeforeResolution = scoreboardRowBeforeResolution,
                teamId = teamId,
                oldIndex = oldIndex,
                currentUnresolvedIndex = currentUnresolvedIndex,
                ranksBeforeResolution = ranking.ranks,
                orderBeforeResolution = ranking.order,
            ).apply {
                currentContestState = currentContestStateResult
                snapshots.add(currentContestState)
                problemIdToResolve?.let { problemId ->
                    (teamIdToProblemIdToFrozenContestStates[teamId]
                        ?: TODO("Unexpected null")).remove(problemId)
                    if (teamIdToProblemIdToFrozenContestStates[teamId]?.isEmpty() == true) {
                        teamIdToProblemIdToFrozenContestStates.remove(teamId)
                    }
                }
                step?.let { step ->
                    steps.add(step)
                }
                currentUnresolvedIndex = currentUnresolvedIndexResult
            }
        }
        return ResolutionResultImpl(
            steps = steps,
            snapshots = snapshots
        )
    }

    private fun analyzePendingCell(
        currentContestState: ContestState,
        frozenContestStates: List<ContestState>,
        teamId: TeamId,
        currentTotalScore: Double,
        currentPenaltyDelta: Duration,
        oldPenalty: Duration,
        currentIsBetterFound: Boolean
    ): CellAnalyzeResult {
        val newRuns = frozenContestStates + currentContestState
        val (rows, ranking) = scoreboardCalculator.calculateScoreboard(
            currentContestState.infoAfterEvent,
            newRuns
        ) ?: TODO("Unexpected null")
        val newIndex = ranking.order.indexOf(teamId)
        val scoreboardRowAfterTestResolution = rows[ranking.order[newIndex]] ?: TODO("Unexpected null")
        val testPenalty = scoreboardRowAfterTestResolution.penalty
        val testTotalScore = scoreboardRowAfterTestResolution.totalScore
        var contestStatesToApply: List<ContestState>? = null
        var currentPenaltyDeltaResult = currentPenaltyDelta
        var currentTotalScoreResult = currentTotalScore
        var currentIsBetterFoundResult = currentIsBetterFound
        if (testTotalScore > currentTotalScore ||
            (testTotalScore == currentTotalScore && testPenalty - oldPenalty < currentPenaltyDeltaResult)
        ) {
            currentTotalScoreResult = testTotalScore
            contestStatesToApply = frozenContestStates
            currentIsBetterFoundResult = true
            currentPenaltyDeltaResult = testPenalty - oldPenalty
        } else if (!currentIsBetterFound) {
            contestStatesToApply = frozenContestStates
        }
        return CellAnalyzeResult(
            contestStatesToApply = contestStatesToApply,
            currentIsBetterFoundResult = currentIsBetterFoundResult,
            currentPenaltyDeltaResult = currentPenaltyDeltaResult,
            currentTotalScoreResult = currentTotalScoreResult,
        )
    }

    private fun prepareDecision(
        currentContestState: ContestState,
        contestStatesToApply: List<ContestState>?,
        scoreboardRowBeforeResolution: ScoreboardRow,
        ranksBeforeResolution: List<Int>,
        orderBeforeResolution: List<TeamId>,
        teamId: TeamId,
        oldIndex: Int,
        currentUnresolvedIndex: Int,
    ): DecisionPreparationResult {
        var currentContestStateResult = currentContestState
        var currentUnresolvedIndexResult = currentUnresolvedIndex
        var resolutionStep: ResolutionStep? = null
        val problemIdToResolve = if (contestStatesToApply != null) {
            ((contestStatesToApply.lastOrNull() ?: TODO("Unexpected null")).lastEvent as RunUpdate).newInfo.problemId
        } else {
            null
        }
        if (contestStatesToApply == null) {
            currentUnresolvedIndexResult--
        } else {
            currentContestStateResult = currentContestStateResult.applyEvents(contestStatesToApply)
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestStateResult)
                ?: TODO("Unexpected null")
            val newIndex = ranking.order.indexOf(teamId)
            val runInfo = (currentContestStateResult.lastEvent as RunUpdate).newInfo
            val ioiResult = runInfo.result as RunResult.IOI
            resolutionStep = if (ioiResult.wrongVerdict != null) {
                ResolutionStep.WithTeamId.IOIRejectResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    index = newIndex,
                    oldRow = scoreboardRowBeforeResolution,
                    row = rows[teamId]!!,
                )
            } else {
                ResolutionStep.WithTeamId.IOIAcceptResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    oldIndex = oldIndex,
                    newIndex = newIndex,
                    row = rows[teamId]!!,
                    ranks = ranking.ranks,
                    order = ranking.order,
                    oldRow = scoreboardRowBeforeResolution,
                    oldRanks = ranksBeforeResolution,
                    oldOrder = orderBeforeResolution,
                )
            }
        }
        return DecisionPreparationResult(
            step = resolutionStep,
            currentContestStateResult = currentContestStateResult,
            problemIdToResolve = problemIdToResolve,
            currentUnresolvedIndexResult = currentUnresolvedIndexResult
        )
    }
}
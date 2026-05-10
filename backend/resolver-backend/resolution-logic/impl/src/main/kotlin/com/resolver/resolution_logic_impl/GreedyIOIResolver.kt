package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.*
import com.resolver.util_api.ScoreboardCalculator
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.*
import kotlin.time.Duration
import com.resolver.util_api.exception.Exception as CoreException

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
        if (states.isEmpty()) {
            throw CoreException.contestStatesIsEmptyException
        }
        val awardIdToTeamIds =
            scoreboardCalculator.getAwardIdToTeamIds(states) ?: throw CoreException.contestInfoIsNullException
        val teamsCount = states.getTeamsCount()
        val runs = states.getRunUpdates()
        val teamIdToProblemIdToFrozenContestStates = runs.getProblemIdToTeamIdToFrozenContestStates()
        var currentContestState = frozenState
        val steps = mutableListOf<ResolutionStep>()
        var currentUnresolvedIndex = teamsCount - 1
        val snapshots = mutableListOf<ContestState>()
        val teamsWhoHaveAtLeastOneResolvedProblem = hashSetOf<TeamId>()
        while (currentUnresolvedIndex >= 0) {
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                ?: throw CoreException.contestInfoIsNullException
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
                    teamId = teamId,
                    teamIndex = currentUnresolvedIndex
                )
                currentUnresolvedIndex--
                continue
            }
            teamsWhoHaveAtLeastOneResolvedProblem.add(teamId)
            var contestStatesToApply: List<ContestState>? = null
            val scoreboardRowBeforeResolution =
                rows[ranking.order[currentUnresolvedIndex]] ?: throw CoreException.teamNotFoundException
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
                        ?: throw CoreException.teamNotFoundException).remove(problemId)
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
        val newRuns = currentContestState + frozenContestStates
        val (rows, ranking) = scoreboardCalculator.calculateScoreboard(
            currentContestState.infoAfterEvent,
            newRuns
        ) ?: throw CoreException.contestInfoIsNullException
        val newIndex = ranking.order.indexOf(teamId)
        val scoreboardRowAfterTestResolution =
            rows[ranking.order[newIndex]] ?: throw CoreException.teamNotFoundException
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
        var resolutionStep: ResolutionStep? = null
        val problemIdToResolve = if (contestStatesToApply != null) {
            ((contestStatesToApply.lastOrNull()
                ?: throw Exception.contestStatesToApplyIsEmptyException).lastEvent as RunUpdate).newInfo.problemId
        } else {
            null
        }
        if (contestStatesToApply != null) {
            currentContestStateResult = currentContestStateResult.applyEvents(contestStatesToApply)
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestStateResult)
                ?: throw CoreException.contestInfoIsNullException
            val newIndex = ranking.order.indexOf(teamId)
            val runInfo = (currentContestStateResult.lastEvent as RunUpdate).newInfo
            val ioiResult = runInfo.result as RunResult.IOI
            resolutionStep = if (ioiResult.wrongVerdict != null) {
                ResolutionStep.WithTeamId.IOIRejectResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    index = newIndex,
                    oldRow = scoreboardRowBeforeResolution,
                    row = rows[teamId] ?: throw CoreException.teamNotFoundException,
                )
            } else {
                ResolutionStep.WithTeamId.IOIAcceptResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    oldIndex = oldIndex,
                    newIndex = newIndex,
                    row = rows[teamId] ?: throw CoreException.teamNotFoundException,
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
            currentUnresolvedIndexResult = currentUnresolvedIndex
        )
    }
}
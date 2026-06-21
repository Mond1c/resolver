package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.AwardBehaviour
import com.resolver.resolution_logic_api.AwardsHandler
import com.resolver.resolution_logic_api.ResolutionResult
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_api.exception.CoreExceptions
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ICPCProblemResult
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.RunResult
import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

@Suppress("DuplicatedCode")
class GreedyICPCResolver(
    private val scoreboardCalculator: ScoreboardCalculator,
    private val awardsHandler: AwardsHandler
) : Resolver {
    private class CellAnalyzeResult(
        val contestStatesToApply: List<ContestState>? = null,
        val isSolvedFoundResult: Boolean,
        val currentPenaltyDeltaResult: Duration,
        val currentMaxSolvedResult: Double
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
            throw CoreExceptions.contestStatesIsEmptyException
        }
        val awardIdToTeamIds =
            scoreboardCalculator.getAwardIdToTeamIds(states)
                ?: throw CoreExceptions.contestInfoIsNullException
        val teamsCount = states.getTeamsCount()
        val runs = states.getRunUpdates()
        val teamIdToProblemIdToFrozenContestStates =
            runs.getProblemIdToTeamIdToFrozenContestStates()
        var currentContestState = frozenState
        val steps = mutableListOf<ResolutionStep>()
        var currentUnresolvedIndex = teamsCount - 1
        val snapshots = mutableListOf<ContestState>()
        val teamsWhoHaveAtLeastOneResolvedProblem = hashSetOf<TeamId>()
        while (currentUnresolvedIndex >= 0) {
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                ?: throw CoreExceptions.contestInfoIsNullException
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
                rows[ranking.order[currentUnresolvedIndex]]
                    ?: throw CoreExceptions.teamNotFoundException
            val oldIndex = currentUnresolvedIndex
            val oldPenalty = scoreboardRowBeforeResolution.penalty
            var currentPenaltyDelta = Duration.INFINITE
            var currentMaxSolved = scoreboardRowBeforeResolution.totalScore
            var isSolvedFound = false
            for (entry in problemIdToFrozenContestStates.entries.toList()) {
                analyzePendingCell(
                    currentContestState = currentContestState,
                    frozenContestStates = entry.value,
                    scoreboardRowBeforeResolution = scoreboardRowBeforeResolution,
                    teamIdToProblemIdToFrozenContestStates = teamIdToProblemIdToFrozenContestStates,
                    problemId = entry.key,
                    teamId = teamId,
                    currentMaxSolved = currentMaxSolved,
                    isSolvedFound = isSolvedFound,
                    currentPenaltyDelta = currentPenaltyDelta,
                    oldPenalty = oldPenalty
                ).apply {
                    isSolvedFound = isSolvedFoundResult
                    currentMaxSolved = currentMaxSolvedResult
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
                ranksBeforeResolution = ranking.ranks,
                orderBeforeResolution = ranking.order,
                teamId = teamId,
                oldIndex = oldIndex,
                currentUnresolvedIndex = currentUnresolvedIndex
            ).apply {
                problemIdToResolve?.let { problemId ->
                    currentContestState = currentContestStateResult
                    snapshots.add(currentContestState)
                    teamIdToProblemIdToFrozenContestStates.removeProblem(teamId, problemId)
                    step?.let { step ->
                        steps.add(step)
                    }
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
        scoreboardRowBeforeResolution: ScoreboardRow,
        teamIdToProblemIdToFrozenContestStates: HashMap<TeamId, HashMap<ProblemId, List<ContestState>>>,
        problemId: ProblemId,
        teamId: TeamId,
        currentMaxSolved: Double,
        isSolvedFound: Boolean,
        currentPenaltyDelta: Duration,
        oldPenalty: Duration
    ): CellAnalyzeResult {
        val newRuns = currentContestState.withRunsFrom(frozenContestStates)
        val (rows, ranking) = scoreboardCalculator.calculateScoreboard(
            currentContestState.infoAfterEvent,
            newRuns
        ) ?: throw CoreExceptions.contestInfoIsNullException
        val newIndex = ranking.order.indexOf(teamId)
        val scoreboardRowAfterTestResolution =
            rows[ranking.order[newIndex]] ?: throw CoreExceptions.teamNotFoundException
        val testPenalty = scoreboardRowAfterTestResolution.penalty
        val testSolved = scoreboardRowAfterTestResolution.totalScore
        var contestStatesToApply: List<ContestState>? = null
        var isSolvedFoundResult = isSolvedFound
        var currentPenaltyDeltaResult = currentPenaltyDelta
        var currentMaxSolvedResult = currentMaxSolved
        if (testSolved > currentMaxSolved) {
            currentMaxSolvedResult = testSolved
            isSolvedFoundResult = true
            contestStatesToApply = frozenContestStates
        } else if (isSolvedFound && testPenalty != oldPenalty && testPenalty - oldPenalty < currentPenaltyDelta) {
            currentPenaltyDeltaResult = testPenalty - oldPenalty
            contestStatesToApply = frozenContestStates
        } else if (!isSolvedFound) {
            val resultBefore = scoreboardRowBeforeResolution
                .problemResults[currentContestState.getProblemOrdinal(problemId)] as ICPCProblemResult
            if (!resultBefore.isSolved) {
                contestStatesToApply = frozenContestStates
            } else {
                teamIdToProblemIdToFrozenContestStates.removeProblem(teamId, problemId)
            }
        }
        return CellAnalyzeResult(
            contestStatesToApply = contestStatesToApply,
            isSolvedFoundResult = isSolvedFoundResult,
            currentPenaltyDeltaResult = currentPenaltyDeltaResult,
            currentMaxSolvedResult = currentMaxSolvedResult
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
                ?: throw ResolverExceptions.contestStatesToApplyIsEmptyException).lastEvent as RunUpdate).newInfo.problemId
        } else {
            null
        }
        if (contestStatesToApply != null) {
            currentContestStateResult = currentContestStateResult.applyEvents(contestStatesToApply)
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestStateResult)
                ?: throw CoreExceptions.contestInfoIsNullException
            val newIndex = ranking.order.indexOf(teamId)
            val runInfo = (currentContestStateResult.lastEvent as RunUpdate).newInfo
            val icpcResult = runInfo.result as RunResult.ICPC
            resolutionStep = if (!icpcResult.verdict.isAccepted) {
                ResolutionStep.WithTeamId.RejectResolutionStep(
                    teamId = runInfo.teamId,
                    row = rows[teamId] ?: throw CoreExceptions.teamNotFoundException,
                    index = oldIndex,
                    problemId = runInfo.problemId,
                    oldRow = scoreboardRowBeforeResolution
                )
            } else {
                ResolutionStep.WithTeamId.AcceptResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    row = rows[teamId] ?: throw CoreExceptions.teamNotFoundException,
                    ranks = ranking.ranks,
                    order = ranking.order,
                    oldRow = scoreboardRowBeforeResolution,
                    oldRanks = ranksBeforeResolution,
                    oldOrder = orderBeforeResolution,
                    oldIndex = oldIndex,
                    newIndex = newIndex
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
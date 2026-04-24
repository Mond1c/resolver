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
        val problemIdToFirstBestSolvedTeamId = runs.getFrozenAmongRunUpdates().getProblemIdToFirstBestSolvedTeamId()
//        val notFrozenContestStates = runs.getNotFrozenAmongRunUpdates()
        val teamIdToProblemIdToFrozenContestStates = runs.getProblemIdToTeamIdToFrozenContestStates()
        var currentContestState = frozenState
        val steps = mutableListOf<ResolutionStep>()
        var currentUnresolvedIndex = teamsCount - 1
        val problemIdToIndex = currentContestState.getProblemIdToIndex() ?: TODO("infoAfterEvent is null")
        val snapshots = mutableListOf<ContestState>()
        while (currentUnresolvedIndex >= 0) {
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                ?: TODO("Unexpected null")
            val teamId = ranking.order[currentUnresolvedIndex]
            val problemIdToFrozenContestStates = teamIdToProblemIdToFrozenContestStates[teamId]
            if (problemIdToFrozenContestStates == null) {
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
            var contestStatesToApply: List<ContestState>? = null
            val scoreboardRowBeforeResolution = rows[ranking.order[currentUnresolvedIndex]] ?: TODO("Unexpected null")
            val oldIndex = currentUnresolvedIndex
            val oldRank = ranking.ranks[oldIndex]
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
                problemIdToFirstBestSolvedTeamId = problemIdToFirstBestSolvedTeamId,
                problemIdToIndex = problemIdToIndex,
                teamId = teamId,
                oldRank = oldRank,
                oldIndex = oldIndex,
                currentUnresolvedIndex = currentUnresolvedIndex
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
        problemIdToIndex: Map<ProblemId, Int>,
        problemIdToFirstBestSolvedTeamId: Map<ProblemId, TeamId>,
        scoreboardRowBeforeResolution: ScoreboardRow,
        teamId: TeamId,
        oldRank: Int,
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
            val newRank = ranking.ranks[newIndex]
            val scoreboardRowAfterResolution = rows[ranking.order[newIndex]] ?: TODO("Unexpected null")
            val runInfo = (currentContestStateResult.lastEvent as RunUpdate).newInfo
            val ioiResult = runInfo.result as RunResult.IOI
            val problemResult = scoreboardRowAfterResolution.problemResults[problemIdToIndex[runInfo.problemId]
                ?: TODO("Unexpected null")] as IOIProblemResult
            resolutionStep = if (ioiResult.wrongVerdict != null) {
                ResolutionStep.WithTeamId.IOIRejectResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    index = newIndex,
                    score = problemResult.score ?: TODO("How is it possible?"),
                    oldTotalScore = scoreboardRowBeforeResolution.totalScore,
                    newTotalScore = scoreboardRowAfterResolution.totalScore,
                    totalAttempts = problemResult.totalAttempts,
                    row = rows[teamId]!!,
                )
            } else {
                ResolutionStep.WithTeamId.IOIAcceptResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    oldRank = oldRank,
                    newRank = newRank,
                    oldIndex = oldIndex,
                    newIndex = newIndex,
                    isFirstBest = problemIdToFirstBestSolvedTeamId[runInfo.problemId] == teamId,
                    totalAttempts = problemResult.totalAttempts,
                    oldTotalPenalty = scoreboardRowBeforeResolution.penalty,
                    newTotalPenalty = scoreboardRowAfterResolution.penalty,
                    score = ioiResult.scoreAfter,
                    oldTotalScore = scoreboardRowBeforeResolution.totalScore,
                    newTotalScore = scoreboardRowAfterResolution.totalScore
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
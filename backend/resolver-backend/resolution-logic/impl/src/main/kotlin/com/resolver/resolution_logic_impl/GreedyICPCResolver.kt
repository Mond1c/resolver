package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.AwardBehaviour
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
        states: List<ContestState>,
        awardIdToAwardBehaviour: Map<String, AwardBehaviour>
    ): ResolutionResult {
        val calculations = scoreboardCalculator.calculateScoreboard(states.last())
        val awards = calculations?.ranks?.awards ?: TODO("Unexpected null")
        val awardIdToTeamIds = HashMap(
            awards
                .groupBy { it.id }
                .mapValues { (it.value.firstOrNull() ?: TODO("Unexpected null")).teams.toHashSet() }
        )
        val teamsCount =
            states.lastOrNull()?.infoAfterEvent?.teams?.size ?: TODO("states is empty or infoAfterEvent is null")
        val runs = states.filter { it.lastEvent is RunUpdate }
        val notFrozenContestStates = runs.filter {
            (it.lastEvent as RunUpdate).newInfo.time < (it.infoAfterEvent?.freezeTime
                ?: TODO("infoAfterEvent or freezeTime is null"))
        }
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
        var currentContestState = notFrozenContestStates.lastOrNull() ?: TODO()
        val steps = mutableListOf<ResolutionStep>()
        var currentUnresolvedIndex = teamsCount - 1
        val problemIdToIndex = currentContestState.infoAfterEvent?.scoreboardProblems?.associate { it.id to it.ordinal }
            ?: TODO("infoAfterEvent is null")
        val snapshots = mutableListOf<ContestState>()
        while (currentUnresolvedIndex >= 0) {
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestState)
                ?: TODO("Unexpected null")
            val teamId = ranking.order[currentUnresolvedIndex]
            val problemIdToFrozenContestStates = teamIdToProblemIdToFrozenContestStates[teamId]
            if (problemIdToFrozenContestStates == null) {
                addAwards(
                    steps = steps,
                    awards = ranking.awards,
                    teamId = teamId,
                    awardIdToAwardBehaviour = awardIdToAwardBehaviour,
                    awardIdToTeamIds = awardIdToTeamIds
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
            var currentMaxSolved = scoreboardRowBeforeResolution.totalScore
            var isSolvedFound = false
            for (entry in problemIdToFrozenContestStates) {
                analyzePendingCell(
                    currentContestState = currentContestState,
                    frozenContestStates = entry.value,
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
        currentMaxSolved: Double,
        isSolvedFound: Boolean,
        currentPenaltyDelta: Duration,
        oldPenalty: Duration
    ): CellAnalyzeResult {
        val newRuns = frozenContestStates.fold(currentContestState.runsAfterEvent) { runs, state ->
            val runInfo = (state.lastEvent as RunUpdate).newInfo
            runs.put(runInfo.id, runInfo)
        }
        val (rows, ranking) = scoreboardCalculator.calculateScoreboard(
            currentContestState.infoAfterEvent,
            newRuns
        ) ?: TODO("Unexpected null")
        val newIndex = ranking.order.indexOf(teamId)
        val scoreboardRowAfterTestResolution = rows[ranking.order[newIndex]] ?: TODO("Unexpected null")
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
            // TODO: problem was solved before freeze, but there are submissions after freeze
            contestStatesToApply = frozenContestStates
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
        problemIdToIndex: Map<ProblemId, Int>,
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
            contestStatesToApply.forEach { contestState ->
                currentContestStateResult = currentContestStateResult.applyEvent(contestState.lastEvent)
            }
            val (rows, ranking) = scoreboardCalculator.calculateScoreboard(currentContestStateResult)
                ?: TODO("Unexpected null")
            val newIndex = ranking.order.indexOf(teamId)
            val newRank = ranking.ranks[newIndex]
            val scoreboardRowAfterResolution = rows[ranking.order[newIndex]] ?: TODO("Unexpected null")
            val runInfo = (currentContestStateResult.lastEvent as RunUpdate).newInfo
            val icpcResult = runInfo.result as RunResult.ICPC
            val problemResult = scoreboardRowAfterResolution.problemResults[problemIdToIndex[runInfo.problemId]
                ?: TODO("Unexpected null")] as ICPCProblemResult
            resolutionStep = if (!icpcResult.verdict.isAccepted) {
                ResolutionStep.WithTeamId.RejectResolutionStep(
                    oldIndex,
                    runInfo.teamId,
                    runInfo.problemId,
                    problemResult.wrongAttempts
                )
            } else {
                ResolutionStep.WithTeamId.ICPCAcceptResolutionStep(
                    teamId = runInfo.teamId,
                    problemId = runInfo.problemId,
                    oldRank = oldRank,
                    newRank = newRank,
                    oldIndex = oldIndex,
                    newIndex = newIndex,
                    isFirstToSolve = problemResult.isFirstToSolve,
                    wrongAttempts = problemResult.wrongAttempts,
                    oldTotalPenalty = scoreboardRowBeforeResolution.penalty,
                    newTotalPenalty = scoreboardRowAfterResolution.penalty
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

    private fun addAwards(
        steps: MutableList<ResolutionStep>,
        awards: List<Award>,
        teamId: TeamId,
        awardIdToAwardBehaviour: Map<String, AwardBehaviour>,
        awardIdToTeamIds: HashMap<String, HashSet<TeamId>>
    ) {
        val teamAwardsToShow = mutableListOf<Award>()
        val groupAwardsToShow = mutableListOf<Award>()
        for (award in awards) {
            if (!award.teams.contains(teamId) || awardIdToAwardBehaviour[award.id] == AwardBehaviour.IGNORE) {
                continue
            }
            (awardIdToTeamIds[award.id] ?: TODO("Unexpected null")).remove(teamId)
            if (
                award.teams.size == 1 ||
                awardIdToAwardBehaviour[award.id] == AwardBehaviour.AFTER_EACH
            ) {
                teamAwardsToShow.add(award)
            } else if (awardIdToTeamIds[award.id]?.isEmpty() ?: TODO("Unexpected null")) {
                groupAwardsToShow.add(award)
            }
        }
        if (teamAwardsToShow.isNotEmpty()) {
            steps.add(
                ResolutionStep.WithTeamId.TeamAwardsResolutionStep(
                    teamId = teamId,
                    awards = teamAwardsToShow
                )
            )
        }
        if (groupAwardsToShow.isNotEmpty()) {
            steps.add(
                ResolutionStep.GroupAwardsResolutionStep(
                    awards = groupAwardsToShow
                )
            )
        }
    }
}
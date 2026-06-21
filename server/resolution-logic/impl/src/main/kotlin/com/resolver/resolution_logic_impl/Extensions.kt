package com.resolver.resolution_logic_impl

import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_api.exception.CoreExceptions
import kotlinx.collections.immutable.PersistentMap
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.applyEvent
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.RunId
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.api.TeamId

internal fun ContestState.applyEvents(contestStatesToApply: List<ContestState>): ContestState {
    return contestStatesToApply.fold(this) { acc, contestState ->
        acc.applyEvent(contestState.lastEvent)
    }
}

internal fun ScoreboardCalculator.getAwardIdToTeamIds(contestStates: List<ContestState>): HashMap<String, HashSet<TeamId>>? {
    val calculations = calculateScoreboard(contestStates.lastOrNull() ?: return null)
    val awards = calculations?.ranks?.awards ?: return null
    return HashMap(
        awards
            .associateBy { it.id }
            .mapValues { (_, award) ->
                award.teams.toHashSet()
            }
    )
}

internal fun List<ContestState>.getRunUpdates() =
    filter { contestState -> contestState.lastEvent is RunUpdate }

internal fun List<ContestState>.getFrozenAmongRunUpdates() =
    when (val freezeTime = lastOrNull()?.infoAfterEvent?.freezeTime) {
        null -> listOf()
        else -> {
            filter {
                (it.lastEvent as RunUpdate).newInfo.time >= freezeTime
            }
        }
    }

internal fun List<ContestState>.getProblemIdToTeamIdToFrozenContestStates(): HashMap<TeamId, HashMap<ProblemId, List<ContestState>>> =
    HashMap(
        getFrozenAmongRunUpdates()
            .groupBy { state ->
                (state.lastEvent as RunUpdate).newInfo.teamId
            }.mapValues { contestStates ->
                HashMap(contestStates.value.groupBy { state ->
                    (state.lastEvent as RunUpdate).newInfo.problemId
                })
            })

internal fun ContestState.withRunsFrom(contestStates: List<ContestState>): PersistentMap<RunId, RunInfo> {
    return contestStates.fold(runsAfterEvent) { runs, state ->
        val runInfo = (state.lastEvent as RunUpdate).newInfo
        runs.put(runInfo.id, runInfo)
    }
}

internal fun List<ContestState>.getTeamsCount() = (last().infoAfterEvent
    ?: throw CoreExceptions.contestInfoIsNullException).teams.size

internal fun ContestState.getProblemOrdinal(problemId: ProblemId) =
    ((infoAfterEvent ?: throw CoreExceptions.contestInfoIsNullException).problems[problemId]
        ?: throw CoreExceptions.problemNotFoundException).ordinal

internal fun HashMap<TeamId, HashMap<ProblemId, List<ContestState>>>.removeProblem(
    teamId: TeamId,
    problemId: ProblemId
) {
    val problems = get(teamId) ?: throw CoreExceptions.teamNotFoundException
    problems.remove(problemId)
    if (problems.isEmpty()) {
        remove(teamId)
    }
}
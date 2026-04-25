package com.resolver.resolution_logic_impl

import com.resolver.util_api.ScoreboardCalculator
import kotlinx.collections.immutable.PersistentMap
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.applyEvent
import org.icpclive.cds.api.*

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
            .groupBy { award -> award.id }
            .mapValues { entries -> (entries.value.firstOrNull() ?: TODO("Unexpected null")).teams.toHashSet() }
    )
}

internal fun List<ContestState>.getRunUpdates() = filter { contestState -> contestState.lastEvent is RunUpdate }

internal fun List<ContestState>.getFrozenAmongRunUpdates() = filter {
    (it.lastEvent as RunUpdate).newInfo.time >= (it.infoAfterEvent?.freezeTime
        ?: TODO("infoAfterEvent or freezeTime is null"))
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

internal operator fun List<ContestState>.plus(contestState: ContestState): PersistentMap<RunId, RunInfo> {
    return fold(contestState.runsAfterEvent) { runs, state ->
        val runInfo = (state.lastEvent as RunUpdate).newInfo
        runs.put(runInfo.id, runInfo)
    }
}
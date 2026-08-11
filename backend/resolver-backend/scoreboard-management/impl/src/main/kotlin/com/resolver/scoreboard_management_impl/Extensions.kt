package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.util_api.exception.CoreExceptions
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.TeamId

internal fun UiEvent.isImportant() = when (this) {
    is UiEvent.AcceptICPC,
    is UiEvent.RejectICPC,
    is UiEvent.AcceptIOI,
    is UiEvent.RejectIOI -> true

    else -> false
}

internal fun List<ResolutionStep>.withTeamIdOrNull(i: Int): ResolutionStep.WithTeamId? {
    val got = get(i)
    if (got is ResolutionStep.WithTeamId.ICPCAcceptResolutionStep ||
        got is ResolutionStep.WithTeamId.IOIAcceptResolutionStep ||
        got is ResolutionStep.WithTeamId.ICPCRejectResolutionStep ||
        got is ResolutionStep.WithTeamId.IOIRejectResolutionStep ||
        got is ResolutionStep.WithTeamId.TeamAwardsResolutionStep
    ) {
        return got
    }
    return null
}

internal fun List<ResolutionStep>.getNextWithTeamIdOrNull(start: Int): ResolutionStep.WithTeamId? {
    for (i in start..<size) {
        withTeamIdOrNull(i)?.let { return it }
    }
    return null
}

internal fun List<ResolutionStep>.getPrevWithTeamIdOrNull(end: Int): ResolutionStep.WithTeamId? {
    for (i in end downTo 0) {
        withTeamIdOrNull(i)?.let { return it }
    }
    return null
}

internal fun List<UiEvent>.findFirstChooseRow(stateIndex: Int, teamId: TeamId): Int? {
    var index = 0
    for (i in 0..<size) {
        if (get(i).isImportant()) {
            index++
        }
        if (index == stateIndex) {
            for (j in (i + 1)..<size) {
                val uiEvent = get(j)
                if (uiEvent is UiEvent.ChooseRow && uiEvent.teamId == teamId) {
                    return j
                }
            }
        }
    }
    return null
}

internal fun ContestState.getProblemDisplayName(problemId: ProblemId): String {
    val infoAfterEvent = infoAfterEvent ?: throw CoreExceptions.contestInfoIsNullException
    val problem = infoAfterEvent.problems[problemId] ?: throw CoreExceptions.problemNotFoundException
    return problem.displayName
}
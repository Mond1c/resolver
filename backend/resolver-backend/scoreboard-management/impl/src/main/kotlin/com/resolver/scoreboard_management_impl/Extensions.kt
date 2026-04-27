package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.UiEvent

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
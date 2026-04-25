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

internal fun List<ResolutionStep>.getNextAcceptOrRejectOrNull(start: Int): ResolutionStep.WithTeamId? {
    if (start + 1 >= size) {
        return null
    }
    for (i in start..<size) {
        val got = get(i)
        if (got is ResolutionStep.WithTeamId.ICPCAcceptResolutionStep ||
            got is ResolutionStep.WithTeamId.IOIAcceptResolutionStep ||
            got is ResolutionStep.WithTeamId.ICPCRejectResolutionStep ||
            got is ResolutionStep.WithTeamId.IOIRejectResolutionStep
        ) {
            return got
        }
    }
    return null
}
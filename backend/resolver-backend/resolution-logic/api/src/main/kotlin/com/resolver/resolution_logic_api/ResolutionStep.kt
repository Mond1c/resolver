package com.resolver.resolution_logic_api

import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

sealed interface ResolutionStep {
    data class RejectResolutionStep(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
    ) : ResolutionStep

    data class ICPCAcceptResolutionStep(
        val teamId: TeamId,
        val problemId: ProblemId,
        val oldRank: Int,
        val newRank: Int,
        val oldIndex: Int,
        val newIndex: Int,
        val isFirstToSolve: Boolean,
        val wrongAttempts: Int,
        val newTotalPenalty: Duration
    ) : ResolutionStep
}
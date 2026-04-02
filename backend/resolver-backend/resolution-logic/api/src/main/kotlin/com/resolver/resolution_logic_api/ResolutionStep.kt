package com.resolver.resolution_logic_api

import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

sealed interface ResolutionStep {
    val teamId: TeamId

    data class RejectResolutionStep(
        val index: Int,
        override val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
    ) : ResolutionStep

    data class ICPCAcceptResolutionStep(
        override val teamId: TeamId,
        val problemId: ProblemId,
        val oldRank: Int,
        val newRank: Int,
        val oldIndex: Int,
        val newIndex: Int,
        val isFirstToSolve: Boolean,
        val wrongAttempts: Int,
        val oldTotalPenalty: Duration,
        val newTotalPenalty: Duration
    ) : ResolutionStep
}
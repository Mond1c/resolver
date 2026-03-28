package com.resolver.resolution_logic_api

import kotlin.time.Duration

sealed interface ResolutionStep {
    data class RejectResolutionStep(
        val teamId: String,
        val problemId: String
    ) : ResolutionStep

    data class ICPCAcceptResolutionStep(
        val teamId: String,
        val problemId: String,
        val oldRank: Int,
        val newRank: Int,
        val oldTotalPenaltyTime: Duration,
        val newTotalPenaltyTime: Duration,
        val isSolved: Boolean,
        val isFirstToSolve: Boolean
    ) : ResolutionStep

    data class IOIAcceptResolutionStep(
        val teamId: String,
        val problemId: String,
        val oldRank: Int,
        val newRank: Int,
        val score: Double,
        val oldTotalScore: Double,
        val newTotalScore: Double
    ) : ResolutionStep
}
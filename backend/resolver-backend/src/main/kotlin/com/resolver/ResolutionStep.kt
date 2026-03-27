package com.resolver

import kotlin.time.Duration

sealed interface ResolutionStep {
    class RejectResolutionStep(
        val teamId: String,
        val problemId: String
    ) : ResolutionStep

    class ICPCAcceptResolutionStep(
        val teamId: String,
        val problemId: String,
        val oldRank: Int,
        val newRank: Int,
        val oldTotalPenaltyTime: Duration,
        val newTotalPenaltyTime: Duration,
        val isSolved: Boolean,
        val isFirstToSolve: Boolean
    ) : ResolutionStep

    class IOIAcceptResolutionStep(
        val teamId: String,
        val problemId: String,
        val oldRank: Int,
        val newRank: Int,
        val score: Double,
        val oldTotalScore: Double,
        val newTotalScore: Double
    ) : ResolutionStep
}
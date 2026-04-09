package com.resolver.resolution_logic_api

import org.icpclive.cds.api.Award
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

sealed interface ResolutionStep {
    sealed interface WithTeamId : ResolutionStep {
        val teamId: TeamId

        data class ICPCRejectResolutionStep(
            override val teamId: TeamId,
            val index: Int,
            val problemId: ProblemId,
            val wrongAttempts: Int
        ) : WithTeamId

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
        ) : WithTeamId

        data class IOIRejectResolutionStep(
            override val teamId: TeamId,
            val index: Int,
            val problemId: ProblemId,
            val score: Double,
            val oldTotalScore: Double,
            val newTotalScore: Double,
            val totalAttempts: Int
        ) : WithTeamId

        data class IOIAcceptResolutionStep(
            override val teamId: TeamId,
            val problemId: ProblemId,
            val oldRank: Int,
            val newRank: Int,
            val oldIndex: Int,
            val newIndex: Int,
            val isFirstBest: Boolean,
            val score: Double,
            val oldTotalScore: Double,
            val newTotalScore: Double,
            val totalAttempts: Int,
            val oldTotalPenalty: Duration,
            val newTotalPenalty: Duration
        ) : WithTeamId

        data class TeamAwardsResolutionStep(
            override val teamId: TeamId,
            val awards: List<Award>
        ) : WithTeamId
    }

    data class GroupAwardsResolutionStep(
        val awards: List<Award>
    ) : ResolutionStep
}
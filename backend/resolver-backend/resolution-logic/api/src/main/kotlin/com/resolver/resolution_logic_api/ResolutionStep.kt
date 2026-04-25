package com.resolver.resolution_logic_api

import org.icpclive.cds.api.Award
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

sealed interface ResolutionStep {
    sealed interface WithTeamId : ResolutionStep {
        val teamId: TeamId

        data class NoResolvedProblemsForTeam(
            override val teamId: TeamId,
            val index: Int
        ) : WithTeamId

        data class ICPCRejectResolutionStep(
            val row: ScoreboardRow,
            override val teamId: TeamId,
            val index: Int,
            val problemId: ProblemId,
            val oldRow: ScoreboardRow
        ) : WithTeamId

        data class ICPCAcceptResolutionStep(
            val row: ScoreboardRow,
            val ranks: List<Int>,
            val order: List<TeamId>,
            override val teamId: TeamId,
            val oldIndex: Int,
            val newIndex: Int,
            val problemId: ProblemId,
            val oldRow: ScoreboardRow,
            val oldRanks: List<Int>,
            val oldOrder: List<TeamId>
        ) : WithTeamId

        data class IOIRejectResolutionStep(
            val row: ScoreboardRow,
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
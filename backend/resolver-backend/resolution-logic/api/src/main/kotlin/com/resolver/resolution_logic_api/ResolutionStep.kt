package com.resolver.resolution_logic_api

import org.icpclive.cds.api.Award
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId

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
            val oldRow: ScoreboardRow
        ) : WithTeamId

        data class IOIAcceptResolutionStep(
            val row: ScoreboardRow,
            val ranks: List<Int>,
            val order: List<TeamId>,
            override val teamId: TeamId,
            val problemId: ProblemId,
            val oldIndex: Int,
            val newIndex: Int,
            val oldRow: ScoreboardRow,
            val oldRanks: List<Int>,
            val oldOrder: List<TeamId>
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
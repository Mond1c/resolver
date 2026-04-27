package com.resolver.scoreboard_management_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.icpclive.cds.api.*

@Serializable
sealed interface UiEvent {
    @Serializable
    @SerialName("ChooseRow")
    data class ChooseRow(
        val index: Int,
        val teamId: TeamId
    ) : UiEvent

    @Serializable
    @SerialName("UnchooseRow")
    data class UnchooseRow(
        val index: Int,
        val teamId: TeamId
    ) : UiEvent

    @Serializable
    @SerialName("ChooseProblem")
    data class ChooseProblem(
        val index: Int,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("UnchooseProblem")
    data class UnchooseProblem(
        val index: Int,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("AcceptICPC")
    data class AcceptICPC(
        val row: ScoreboardRow,
        val ranks: List<Int>,
        val order: List<TeamId>,
        @Transient val oldRow: ScoreboardRow? = null,
        @Transient val oldRanks: List<Int>? = null,
        @Transient val oldOrder: List<TeamId>? = null,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ReverseAcceptICPC")
    data class ReverseAcceptICPC(
        val oldRow: ScoreboardRow,
        val oldRanks: List<Int>,
        val oldOrder: List<TeamId>,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("RejectICPC")
    data class RejectICPC(
        val row: ScoreboardRow,
        @Transient val oldRow: ScoreboardRow? = null,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ReverseRejectICPC")
    data class ReverseRejectICPC(
        val oldRow: ScoreboardRow,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("AcceptIOI")
    data class AcceptIOI(
        val row: ScoreboardRow,
        val ranks: List<Int>,
        val order: List<TeamId>,
        @Transient val oldRow: ScoreboardRow? = null,
        @Transient val oldRanks: List<Int>? = null,
        @Transient val oldOrder: List<TeamId>? = null,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ReverseAcceptIOI")
    data class ReverseAcceptIOI(
        val oldRow: ScoreboardRow,
        val oldRanks: List<Int>,
        val oldOrder: List<TeamId>,
        val teamId: TeamId,
        val problemId: ProblemId,
    ) : UiEvent

    @Serializable
    @SerialName("RejectIOI")
    data class RejectIOI(
        val row: ScoreboardRow,
        @Transient val oldRow: ScoreboardRow? = null,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ReverseRejectIOI")
    data class ReverseRejectIOI(
        val oldRow: ScoreboardRow,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ShowTeamAwards")
    data class ShowTeamAwards(
        val teamId: TeamId,
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("HideTeamAwards")
    data class HideTeamAwards(
        val teamId: TeamId,
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("ShowGroupAwards")
    data class ShowGroupAwards(
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("HideGroupAwards")
    data class HideGroupAwards(
        val awards: List<Award>
    ) : UiEvent

    @Suppress("UNUSED")
    @Serializable
    @SerialName("Scoreboard")
    class Scoreboard(
        val teamIdToScoreboardRow: Map<TeamId, ScoreboardRow>,
        val order: List<TeamId>,
        val ranks: List<Int>,
        val contestInfo: ContestInfo,
        val indexOfLastChosenRow: Int?,
        val teamOfLastChosenRow: TeamId?,
        val isLastChosenRowChosenNow: Boolean
    ) : UiEvent

    @Serializable
    @SerialName("NoOp")
    object NoOp : UiEvent
}
package com.resolver.scoreboard_management_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.icpclive.cds.api.Award
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

@Serializable
sealed interface UiEvent {
    @Serializable
    @SerialName("ChooseRow")
    data class ChooseRow(
        val index: Int
    ) : UiEvent

    @Serializable
    @SerialName("UnchooseRow")
    data class UnchooseRow(
        val index: Int
    ) : UiEvent

    @Serializable
    @SerialName("ChooseProblem")
    data class ChooseProblem(
        val index: Int,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("UnchooseProblem")
    data class UnchooseProblem(
        val index: Int,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("AcceptICPC")
    data class AcceptICPC(
        val teamId: TeamId,
        val problemId: ProblemId,
        val oldRank: Int,
        val newRank: Int,
        val oldIndex: Int,
        val newIndex: Int,
        val newTotalPenalty: Duration,
        val oldTotalPenalty: Duration,
        val wrongAttempts: Int,
        val isFirstToSolve: Boolean
    ) : UiEvent

    @Serializable
    @SerialName("ReverseAcceptICPC")
    data class ReverseAcceptICPC(
        val teamId: TeamId,
        val problemId: ProblemId,
        val oldRank: Int,
        val newRank: Int,
        val oldIndex: Int,
        val newIndex: Int,
        val wrongAttempts: Int,
        val newTotalPenalty: Duration
    ) : UiEvent

    @Serializable
    @SerialName("RejectICPC")
    data class RejectICPC(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int // TODO: oldWrongAttempts newWrongAttempts ???
    ) : UiEvent

    @Serializable
    @SerialName("ReverseRejectICPC")
    data class ReverseRejectICPC(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
    ) : UiEvent

    @Serializable
    @SerialName("AcceptIOI")
    data class AcceptIOI(
        val teamId: TeamId,
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
    ) : UiEvent

    @Serializable
    @SerialName("ReverseAcceptIOI")
    data class ReverseAcceptIOI(
        val teamId: TeamId,
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
    ) : UiEvent

    @Serializable
    @SerialName("RejectIOI")
    data class RejectIOI(
        val teamId: TeamId,
        val index: Int,
        val problemId: ProblemId,
        val score: Double,
        val oldTotalScore: Double,
        val newTotalScore: Double,
        val totalAttempts: Int
    ) : UiEvent

    @Serializable
    @SerialName("ReverseRejectIOI")
    data class ReverseRejectIOI(
        val teamId: TeamId,
        val index: Int,
        val problemId: ProblemId,
        val score: Double,
        val oldTotalScore: Double,
        val newTotalScore: Double,
        val totalAttempts: Int
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

    @Serializable
    @SerialName("NoOp")
    object NoOp : UiEvent
}
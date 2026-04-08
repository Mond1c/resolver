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
    @SerialName("Reject")
    data class Reject(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
    ) : UiEvent

    @Serializable
    @SerialName("ReverseReject")
    data class ReverseReject(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
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
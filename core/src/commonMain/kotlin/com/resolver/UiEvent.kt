package com.resolver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface UiEvent {
    sealed interface AcceptOrReverse : UiEvent {
        val row: ScoreboardRow
        val ranks: List<Int>
        val order: List<TeamId>
        val teamId: TeamId
        val problemId: ProblemId
    }

    sealed interface RejectOrReverse : UiEvent {
        val row: ScoreboardRow
        val teamId: TeamId
        val problemId: ProblemId
    }

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
    @SerialName("Accept")
    data class Accept(
        override val row: ScoreboardRow,
        override val ranks: List<Int>,
        override val order: List<TeamId>,
        override val teamId: TeamId,
        override val problemId: ProblemId
    ) : AcceptOrReverse

    @Serializable
    @SerialName("ReverseAccept")
    data class ReverseAccept(
        override val row: ScoreboardRow,
        override val ranks: List<Int>,
        override val order: List<TeamId>,
        override val teamId: TeamId,
        override val problemId: ProblemId
    ) : AcceptOrReverse

    @Serializable
    @SerialName("Reject")
    data class Reject(
        override val row: ScoreboardRow,
        override val teamId: TeamId,
        override val problemId: ProblemId
    ) : RejectOrReverse

    @Serializable
    @SerialName("ReverseReject")
    data class ReverseReject(
        override val row: ScoreboardRow,
        override val teamId: TeamId,
        override val problemId: ProblemId
    ) : RejectOrReverse

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
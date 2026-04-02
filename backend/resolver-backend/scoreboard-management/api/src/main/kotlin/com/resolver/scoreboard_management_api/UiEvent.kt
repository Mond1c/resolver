package com.resolver.scoreboard_management_api

import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.TeamId
import kotlin.time.Duration

sealed interface UiEvent {
    data class ChooseRow(
        val index: Int
    ) : UiEvent

    data class UnchooseRow(
        val index: Int
    ) : UiEvent

    data class ChooseProblem(
        val index: Int,
        val problemId: ProblemId
    ) : UiEvent

    data class UnchooseProblem(
        val index: Int,
        val problemId: ProblemId
    ) : UiEvent

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

    data class Reject(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
    ) : UiEvent

    data class ReverseReject(
        val teamId: TeamId,
        val problemId: ProblemId,
        val wrongAttempts: Int
    ) : UiEvent

    object NoOp : UiEvent
}
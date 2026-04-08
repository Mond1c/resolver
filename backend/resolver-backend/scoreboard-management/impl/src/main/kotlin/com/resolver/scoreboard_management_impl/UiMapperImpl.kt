package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_api.UiMapper

object UiMapperImpl : UiMapper {
    override fun mapToUiEvents(steps: List<ResolutionStep>): List<UiEvent> {
        var isPrevTheSame = false
        return buildList {
            for (i in 0..<steps.size) {
                when (val step = steps[i]) {
                    is ResolutionStep.WithTeamId.ICPCAcceptResolutionStep -> {
                        isPrevTheSame = handleICPCAcceptResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getOrNull(i + 1) as? ResolutionStep.WithTeamId
                        )
                    }

                    is ResolutionStep.WithTeamId.RejectResolutionStep -> {
                        isPrevTheSame = handleRejectResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getOrNull(i + 1) as? ResolutionStep.WithTeamId
                        )
                    }

                    is ResolutionStep.GroupAwardsResolutionStep -> {
                        isPrevTheSame = handleGroupAwardsResolutionStep(step)
                    }

                    is ResolutionStep.WithTeamId.TeamAwardsResolutionStep -> {
                        isPrevTheSame = handleTeamAwardsResolutionStep(step)
                    }
                }
            }
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.ICPCAcceptResolutionStep): UiEvent {
        return with(step) {
            UiEvent.AcceptICPC(
                teamId = teamId,
                problemId = problemId,
                oldRank = oldRank,
                newRank = newRank,
                oldIndex = oldIndex,
                newIndex = newIndex,
                newTotalPenalty = newTotalPenalty,
                oldTotalPenalty = oldTotalPenalty,
                wrongAttempts = wrongAttempts,
                isFirstToSolve = isFirstToSolve
            )
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.RejectResolutionStep): UiEvent {
        return with(step) {
            UiEvent.Reject(
                teamId = teamId,
                problemId = problemId,
                wrongAttempts = wrongAttempts
            )
        }
    }

    override fun reverse(uiEvent: UiEvent): UiEvent {
        return when (uiEvent) {
            is UiEvent.AcceptICPC -> {
                with(uiEvent) {
                    UiEvent.ReverseAcceptICPC(
                        teamId = teamId,
                        problemId = problemId,
                        oldRank = newRank,
                        newRank = oldRank,
                        oldIndex = newIndex,
                        newIndex = oldIndex,
                        wrongAttempts = wrongAttempts,
                        newTotalPenalty = oldTotalPenalty
                    )
                }
            }

            is UiEvent.ChooseProblem -> UiEvent.UnchooseProblem(
                index = uiEvent.index,
                problemId = uiEvent.problemId
            )

            is UiEvent.ChooseRow -> UiEvent.UnchooseRow(
                index = uiEvent.index
            )

            is UiEvent.Reject -> UiEvent.ReverseReject(
                teamId = uiEvent.teamId,
                problemId = uiEvent.problemId,
                wrongAttempts = uiEvent.wrongAttempts - 1
            )

            is UiEvent.UnchooseRow -> UiEvent.ChooseRow(
                index = uiEvent.index
            )

            is UiEvent.UnchooseProblem -> UiEvent.ChooseProblem(
                index = uiEvent.index,
                problemId = uiEvent.problemId
            )

            is UiEvent.ShowTeamAwards -> UiEvent.HideTeamAwards(
                teamId = uiEvent.teamId,
                awards = uiEvent.awards
            )

            is UiEvent.HideTeamAwards -> UiEvent.ShowTeamAwards(
                teamId = uiEvent.teamId,
                awards = uiEvent.awards
            )

            is UiEvent.ShowGroupAwards -> UiEvent.HideGroupAwards(
                awards = uiEvent.awards
            )

            is UiEvent.HideGroupAwards -> UiEvent.ShowGroupAwards(
                awards = uiEvent.awards
            )

            else -> UiEvent.NoOp
        }
    }

    private fun MutableList<UiEvent>.handleICPCAcceptResolutionStep(
        step: ResolutionStep.WithTeamId.ICPCAcceptResolutionStep,
        currentIsPrevTheSame: Boolean,
        nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (!currentIsPrevTheSame) {
            add(UiEvent.ChooseRow(index = step.oldIndex))
        }
        add(
            UiEvent.ChooseProblem(
                index = step.oldIndex,
                problemId = step.problemId
            )
        )
        add(this@UiMapperImpl mapToUiEvent step)
        add(
            UiEvent.UnchooseProblem(
                index = step.newIndex,
                problemId = step.problemId
            )
        )
        return if (nextOrNull?.teamId == step.teamId) {
            true
        } else {
            add(UiEvent.UnchooseRow(index = step.oldIndex))
            false
        }
    }

    private fun MutableList<UiEvent>.handleRejectResolutionStep(
        step: ResolutionStep.WithTeamId.RejectResolutionStep,
        currentIsPrevTheSame: Boolean,
        nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (!currentIsPrevTheSame) {
            add(UiEvent.ChooseRow(index = step.index))
        }
        add(
            UiEvent.ChooseProblem(
                index = step.index,
                problemId = step.problemId
            )
        )
        add(this@UiMapperImpl mapToUiEvent step)
        add(
            UiEvent.UnchooseProblem(
                index = step.index,
                problemId = step.problemId
            )
        )
        return if (nextOrNull?.teamId == step.teamId) {
            true
        } else {
            add(UiEvent.UnchooseRow(index = step.index))
            false
        }
    }

    private fun MutableList<UiEvent>.handleTeamAwardsResolutionStep(
        step: ResolutionStep.WithTeamId.TeamAwardsResolutionStep
    ): Boolean {
        add(
            UiEvent.ShowTeamAwards(
                teamId = step.teamId,
                awards = step.awards
            )
        )
        add(
            UiEvent.HideTeamAwards(
                teamId = step.teamId,
                awards = step.awards
            )
        )
        return false
    }

    private fun MutableList<UiEvent>.handleGroupAwardsResolutionStep(
        step: ResolutionStep.GroupAwardsResolutionStep
    ): Boolean {
        add(
            UiEvent.ShowGroupAwards(
                awards = step.awards
            )
        )
        add(
            UiEvent.HideGroupAwards(
                awards = step.awards
            )
        )
        return false
    }
}
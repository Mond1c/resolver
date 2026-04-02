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
                    is ResolutionStep.ICPCAcceptResolutionStep -> {
                        if (!isPrevTheSame) {
                            add(UiEvent.ChooseRow(index = step.oldIndex))
                        }
                        add(
                            UiEvent.ChooseProblem(
                                index = step.oldIndex,
                                problemId = step.problemId
                            )
                        )
                        add(this@UiMapperImpl mapToUiEvent step)
                        if (i < steps.size - 1 && steps[i + 1].teamId == step.teamId) {
                            isPrevTheSame = true
                        } else {
                            isPrevTheSame = false
                            add(UiEvent.UnchooseRow(index = step.oldIndex))
                        }
                    }

                    is ResolutionStep.RejectResolutionStep -> {
                        if (!isPrevTheSame) {
                            add(UiEvent.ChooseRow(index = step.index))
                        }
                        add(
                            UiEvent.ChooseProblem(
                                index = step.index,
                                problemId = step.problemId
                            )
                        )
                        add(this@UiMapperImpl mapToUiEvent step)
                        if (i < steps.size - 1 && steps[i + 1].teamId == step.teamId) {
                            isPrevTheSame = true
                        } else {
                            isPrevTheSame = false
                            add(UiEvent.UnchooseRow(index = step.index))
                        }
                    }
                }
            }
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.ICPCAcceptResolutionStep): UiEvent {
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

    override fun mapToUiEvent(step: ResolutionStep.RejectResolutionStep): UiEvent {
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

            else -> UiEvent.NoOp
        }
    }
}
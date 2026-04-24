package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_api.UiMapper

@Suppress("DuplicatedCode")
object UiMapperImpl : UiMapper {
    override fun mapToUiEvents(steps: List<ResolutionStep>): List<UiEvent> {
        var isPrevTheSame = false
        return buildList {
            for (i in 0..<steps.size) {
                isPrevTheSame = when (val step = steps[i]) {
                    is ResolutionStep.WithTeamId.ICPCAcceptResolutionStep -> {
                        handleICPCAcceptResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getOrNull(i + 1) as? ResolutionStep.WithTeamId
                        )
                    }

                    is ResolutionStep.WithTeamId.ICPCRejectResolutionStep -> {
                        handleICPCRejectResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getOrNull(i + 1) as? ResolutionStep.WithTeamId
                        )
                    }

                    is ResolutionStep.GroupAwardsResolutionStep -> {
                        handleGroupAwardsResolutionStep(step)
                    }

                    is ResolutionStep.WithTeamId.TeamAwardsResolutionStep -> {
                        handleTeamAwardsResolutionStep(step)
                    }

                    is ResolutionStep.WithTeamId.IOIAcceptResolutionStep -> {
                        handleIOIAcceptResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getOrNull(i + 1) as? ResolutionStep.WithTeamId
                        )
                    }

                    is ResolutionStep.WithTeamId.IOIRejectResolutionStep -> {
                        handleIOIRejectResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getOrNull(i + 1) as? ResolutionStep.WithTeamId
                        )
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
                isFirstToSolve = isFirstToSolve,
                row = row,
                ranks = ranks,
                order = order
            )
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.ICPCRejectResolutionStep): UiEvent {
        return with(step) {
            UiEvent.RejectICPC(
                teamId = teamId,
                problemId = problemId,
                wrongAttempts = wrongAttempts,
                row = row
            )
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.IOIAcceptResolutionStep): UiEvent {
        return with(step) {
            UiEvent.AcceptIOI(
                teamId = teamId,
                problemId = problemId,
                oldRank = oldRank,
                newRank = newRank,
                oldIndex = oldIndex,
                newIndex = newIndex,
                isFirstBest = isFirstBest,
                score = score,
                oldTotalScore = oldTotalScore,
                newTotalScore = newTotalScore,
                totalAttempts = totalAttempts,
                oldTotalPenalty = oldTotalPenalty,
                newTotalPenalty = newTotalPenalty
            )
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.IOIRejectResolutionStep): UiEvent {
        return with(step) {
            UiEvent.RejectIOI(
                teamId = teamId,
                index = index,
                problemId = problemId,
                score = score,
                oldTotalScore = oldTotalScore,
                newTotalScore = newTotalScore,
                totalAttempts = totalAttempts
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
                problemId = uiEvent.problemId,
                teamId = uiEvent.teamId,
            )

            is UiEvent.ChooseRow -> UiEvent.UnchooseRow(
                index = uiEvent.index,
                teamId = uiEvent.teamId
            )

            is UiEvent.RejectICPC -> UiEvent.ReverseRejectICPC(
                teamId = uiEvent.teamId,
                problemId = uiEvent.problemId,
                wrongAttempts = uiEvent.wrongAttempts - 1 // TODO
            )

            is UiEvent.UnchooseRow -> UiEvent.ChooseRow(
                index = uiEvent.index,
                teamId = uiEvent.teamId 
            )

            is UiEvent.UnchooseProblem -> UiEvent.ChooseProblem(
                index = uiEvent.index,
                problemId = uiEvent.problemId,
                teamId = uiEvent.teamId,
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

            is UiEvent.AcceptIOI -> {
                with(uiEvent) {
                    UiEvent.ReverseAcceptIOI(
                        teamId = teamId,
                        problemId = problemId,
                        oldRank = newRank,
                        newRank = oldRank,
                        oldIndex = newIndex,
                        newIndex = oldIndex,
                        isFirstBest = false, // TODO: first best is possible before freeze ???
                        score = score,
                        oldTotalScore = newTotalScore,
                        newTotalScore = oldTotalScore,
                        totalAttempts = totalAttempts - 1, // TODO: use oldTotalAttempts and newTotalAttempts instead ???
                        oldTotalPenalty = newTotalPenalty,
                        newTotalPenalty = oldTotalPenalty
                    )
                }
            }

            is UiEvent.RejectIOI -> {
                with(uiEvent) {
                    UiEvent.ReverseRejectIOI(
                        teamId = teamId,
                        index = index,
                        problemId = problemId,
                        score = score,
                        oldTotalScore = newTotalScore,
                        newTotalScore = oldTotalScore,
                        totalAttempts = totalAttempts - 1 // TODO
                    )
                }
            }

            else -> UiEvent.NoOp
        }
    }

    private fun MutableList<UiEvent>.handleICPCAcceptResolutionStep(
        step: ResolutionStep.WithTeamId.ICPCAcceptResolutionStep,
        currentIsPrevTheSame: Boolean,
        nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (!currentIsPrevTheSame) {
            add(UiEvent.ChooseRow(index = step.oldIndex, teamId = step.teamId))
        }
        add(
            UiEvent.ChooseProblem(
                index = step.oldIndex,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        add(this@UiMapperImpl mapToUiEvent step)
        add(
            UiEvent.UnchooseProblem(
                index = step.newIndex,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        return if (nextOrNull?.teamId == step.teamId) {
            true
        } else {
            add(UiEvent.UnchooseRow(index = step.oldIndex, teamId = step.teamId))
            false
        }
    }

    private fun MutableList<UiEvent>.handleIOIAcceptResolutionStep(
        step: ResolutionStep.WithTeamId.IOIAcceptResolutionStep,
        currentIsPrevTheSame: Boolean,
        nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (!currentIsPrevTheSame) {
            add(UiEvent.ChooseRow(index = step.oldIndex, teamId = step.teamId))
        }
        add(
            UiEvent.ChooseProblem(
                index = step.oldIndex,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        add(this@UiMapperImpl mapToUiEvent step)
        add(
            UiEvent.UnchooseProblem(
                index = step.newIndex,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        return if (nextOrNull?.teamId == step.teamId) {
            true
        } else {
            add(UiEvent.UnchooseRow(index = step.oldIndex, teamId = step.teamId))
            false
        }
    }

    private fun MutableList<UiEvent>.handleICPCRejectResolutionStep(
        step: ResolutionStep.WithTeamId.ICPCRejectResolutionStep,
        currentIsPrevTheSame: Boolean,
        nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (!currentIsPrevTheSame) {
            add(UiEvent.ChooseRow(index = step.index, teamId = step.teamId))
        }
        add(
            UiEvent.ChooseProblem(
                index = step.index,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        add(this@UiMapperImpl mapToUiEvent step)
        add(
            UiEvent.UnchooseProblem(
                index = step.index,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        return if (nextOrNull?.teamId == step.teamId) {
            true
        } else {
            add(UiEvent.UnchooseRow(index = step.index, teamId = step.teamId))
            false
        }
    }

    private fun MutableList<UiEvent>.handleIOIRejectResolutionStep(
        step: ResolutionStep.WithTeamId.IOIRejectResolutionStep,
        currentIsPrevTheSame: Boolean,
        nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (!currentIsPrevTheSame) {
            add(UiEvent.ChooseRow(index = step.index, teamId = step.teamId))
        }
        add(
            UiEvent.ChooseProblem(
                index = step.index,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        add(this@UiMapperImpl mapToUiEvent step)
        add(
            UiEvent.UnchooseProblem(
                index = step.index,
                problemId = step.problemId,
                teamId = step.teamId,
            )
        )
        return if (nextOrNull?.teamId == step.teamId) {
            true
        } else {
            add(UiEvent.UnchooseRow(index = step.index, teamId = step.teamId))
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
package com.resolver.scoreboard_management_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_api.UiMapper

object UiMapperImpl : UiMapper {
    override fun mapToUiEvents(steps: List<ResolutionStep>): List<UiEvent> {
        var isPrevTheSame = false
        return buildList {
            for (i in steps.indices) {
                isPrevTheSame = when (val step = steps[i]) {
                    is ResolutionStep.WithTeamId.AcceptResolutionStep -> {
                        handleAcceptResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getNextWithTeamIdOrNull(i + 1)
                        )
                    }

                    is ResolutionStep.WithTeamId.RejectResolutionStep -> {
                        handleRejectResolutionStep(
                            step = step,
                            currentIsPrevTheSame = isPrevTheSame,
                            nextOrNull = steps.getNextWithTeamIdOrNull(i + 1)
                        )
                    }

                    is ResolutionStep.GroupAwardsResolutionStep -> {
                        handleGroupAwardsResolutionStep(step)
                    }

                    is ResolutionStep.WithTeamId.TeamAwardsResolutionStep -> {
                        handleTeamAwardsResolutionStep(
                            step = step,
                            prevOrNull = steps.getPrevWithTeamIdOrNull(i - 1)
                        )
                    }

                    is ResolutionStep.WithTeamId.NoResolvedProblemsForTeam -> {
                        handleNoResolvedProblemsForTeam(
                            step = step, nextOrNull = steps.getNextWithTeamIdOrNull(i + 1)
                        )
                    }
                }
            }
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.AcceptResolutionStep): UiEvent {
        return with(step) {
            UiEvent.Accept(
                teamId = teamId,
                problemId = problemId,
                row = row,
                ranks = ranks,
                order = order,
                oldRow = oldRow,
                oldRanks = oldRanks,
                oldOrder = oldOrder
            )
        }
    }

    override fun mapToUiEvent(step: ResolutionStep.WithTeamId.RejectResolutionStep): UiEvent {
        return with(step) {
            UiEvent.Reject(
                teamId = teamId, problemId = problemId, row = row, oldRow = oldRow
            )
        }
    }

    override fun reverse(uiEvent: UiEvent): UiEvent {
        return when (uiEvent) {
            is UiEvent.Accept -> {
                with(uiEvent) {
                    UiEvent.ReverseAccept(
                        teamId = teamId,
                        problemId = problemId,
                        oldRow = oldRow!!,
                        oldRanks = oldRanks!!,
                        oldOrder = oldOrder!!,
                    )
                }
            }

            is UiEvent.ChooseProblem -> UiEvent.UnchooseProblem(
                index = uiEvent.index,
                problemId = uiEvent.problemId,
                teamId = uiEvent.teamId,
            )

            is UiEvent.ChooseRow -> UiEvent.UnchooseRow(
                index = uiEvent.index, teamId = uiEvent.teamId
            )

            is UiEvent.Reject -> UiEvent.ReverseReject(
                teamId = uiEvent.teamId, problemId = uiEvent.problemId, oldRow = uiEvent.oldRow!!
            )

            is UiEvent.UnchooseRow -> UiEvent.ChooseRow(
                index = uiEvent.index, teamId = uiEvent.teamId
            )

            is UiEvent.UnchooseProblem -> UiEvent.ChooseProblem(
                index = uiEvent.index,
                problemId = uiEvent.problemId,
                teamId = uiEvent.teamId,
            )

            is UiEvent.ShowTeamAwards -> UiEvent.HideTeamAwards(
                teamId = uiEvent.teamId, awards = uiEvent.awards
            )

            is UiEvent.HideTeamAwards -> UiEvent.ShowTeamAwards(
                teamId = uiEvent.teamId, awards = uiEvent.awards
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

    private fun MutableList<UiEvent>.handleAcceptResolutionStep(
        step: ResolutionStep.WithTeamId.AcceptResolutionStep,
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

    private fun MutableList<UiEvent>.handleRejectResolutionStep(
        step: ResolutionStep.WithTeamId.RejectResolutionStep,
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
        step: ResolutionStep.WithTeamId.TeamAwardsResolutionStep,
        prevOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (prevOrNull?.teamId != step.teamId) {
            add(
                UiEvent.ChooseRow(
                    index = step.teamIndex, teamId = step.teamId
                )
            )
        }
        add(
            UiEvent.ShowTeamAwards(
                teamId = step.teamId, awards = step.awards
            )
        )
        add(
            UiEvent.HideTeamAwards(
                teamId = step.teamId, awards = step.awards
            )
        )
        add(
            UiEvent.UnchooseRow(
                index = step.teamIndex, teamId = step.teamId
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

    private fun MutableList<UiEvent>.handleNoResolvedProblemsForTeam(
        step: ResolutionStep.WithTeamId.NoResolvedProblemsForTeam, nextOrNull: ResolutionStep.WithTeamId?
    ): Boolean {
        if (step.teamId != nextOrNull?.teamId) {
            add(UiEvent.ChooseRow(index = step.index, teamId = step.teamId))
            add(UiEvent.UnchooseRow(index = step.index, teamId = step.teamId))
        }
        return false
    }
}
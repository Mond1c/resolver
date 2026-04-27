package com.resolver.scoreboard_management_impl

import com.resolver.scoreboard_management_api.UiEvent
import org.icpclive.cds.api.ContestResultType
import org.slf4j.LoggerFactory

object UiEventSequenceValidator {
    private val logger = LoggerFactory.getLogger(UiEventSequenceValidator::class.java)

    fun isUiEventSequenceValid(
        uiEvents: List<UiEvent>,
        contestResultType: ContestResultType
    ): Boolean {
        if (uiEvents.isEmpty()) {
            return true
        }
        fun log(i: Int) =
            logger.info(
                uiEvents
                    .safeSublist(i)
                    .map { it::class }
                    .joinToString(System.lineSeparator())
            )
        if (uiEvents[0] !is UiEvent.ChooseRow) {
            return false
        }
        when (uiEvents.last()) {
            is UiEvent.UnchooseRow, is UiEvent.HideGroupAwards -> {}
            else -> {
                log(uiEvents.size)
                return false
            }
        }
        for (i in 0..<uiEvents.size - 1) {
            when (uiEvents[i]) {
                is UiEvent.AcceptICPC,
                is UiEvent.AcceptIOI,
                is UiEvent.RejectICPC,
                is UiEvent.RejectIOI -> {
                    if (uiEvents[i + 1] !is UiEvent.UnchooseProblem) {
                        log(i)
                        return false
                    }
                }

                is UiEvent.ChooseProblem -> {
                    when (contestResultType) {
                        ContestResultType.ICPC -> {
                            when (uiEvents[i + 1]) {
                                is UiEvent.AcceptICPC, is UiEvent.RejectICPC -> {}
                                else -> {
                                    log(i)
                                    return false
                                }
                            }
                        }

                        ContestResultType.IOI -> {
                            when (uiEvents[i + 1]) {
                                is UiEvent.AcceptIOI, is UiEvent.RejectIOI -> {}
                                else -> {
                                    log(i)
                                    return false
                                }
                            }
                        }
                    }
                }

                is UiEvent.ChooseRow -> {
                    when (uiEvents[i + 1]) {
                        is UiEvent.ChooseProblem, is UiEvent.UnchooseRow, is UiEvent.ShowTeamAwards -> {}
                        else -> {
                            log(i)
                            return false
                        }
                    }
                }

                is UiEvent.HideGroupAwards -> {
                    if (uiEvents[i + 1] !is UiEvent.ChooseRow) {
                        log(i)
                        return false
                    }
                }

                is UiEvent.HideTeamAwards -> {
                    if (uiEvents[i + 1] !is UiEvent.UnchooseRow) {
                        log(i)
                        return false
                    }
                }

                is UiEvent.ShowGroupAwards -> {
                    if (uiEvents[i + 1] !is UiEvent.HideGroupAwards) {
                        log(i)
                        return false
                    }
                }

                is UiEvent.ShowTeamAwards -> {
                    if (uiEvents[i + 1] !is UiEvent.HideTeamAwards) {
                        log(i)
                        return false
                    }
                }

                is UiEvent.UnchooseProblem -> {
                    when (uiEvents[i + 1]) {
                        is UiEvent.ShowTeamAwards, is UiEvent.UnchooseRow, is UiEvent.ChooseProblem -> {}
                        else -> {
                            log(i)
                            return false
                        }
                    }
                }

                is UiEvent.UnchooseRow -> {
                    when (uiEvents[i + 1]) {
                        is UiEvent.ChooseRow, is UiEvent.ShowGroupAwards -> {}
                        else -> {
                            println((uiEvents[i + 1] as UiEvent.ShowTeamAwards).teamId)
                            log(i)
                            return false
                        }
                    }
                }

                else -> {}
            }

        }
        return true
    }
}
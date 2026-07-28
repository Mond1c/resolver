package org.icpclive.resolver.scoreboard_management_impl

import org.icpclive.resolver.scoreboard_management_api.UiEvent
import org.slf4j.LoggerFactory

object UiEventSequenceValidator {
    private val logger = LoggerFactory.getLogger(UiEventSequenceValidator::class.java)

    fun isUiEventSequenceValid(
        uiEvents: List<UiEvent>
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
                is UiEvent.Accept,
                is UiEvent.Reject -> {
                    if (uiEvents[i + 1] !is UiEvent.UnchooseProblem) {
                        log(i)
                        return false
                    }
                }

                is UiEvent.ChooseProblem -> {
                    when (uiEvents[i + 1]) {
                        is UiEvent.Accept, is UiEvent.Reject -> {}
                        else -> {
                            log(i)
                            return false
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
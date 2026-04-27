package com.resolver.scoreboard_management_impl

import com.resolver.scoreboard_management_api.UiEvent

object UiEventSequenceValidator {
    fun isICPCUiEventSequenceValid(uiEvents: List<UiEvent>): Boolean {
        for (i in uiEvents.indices) {
            if (i + 1 == uiEvents.size) {
                return when (uiEvents[i]) {
                    is UiEvent.UnchooseRow, is UiEvent.HideGroupAwards -> true
                    else -> false
                }
            }
            if (uiEvents[i] is UiEvent.ChooseRow) {
                when (uiEvents[i]) {
                    is UiEvent.ChooseRow, is UiEvent.UnchooseRow -> {}
                    else -> return false
                }
            } else if (uiEvents[i] is UiEvent.HideGroupAwards) {
                if (uiEvents[i] !is UiEvent.ChooseRow) {
                    return false
                }
            } else if (uiEvents[i] is UiEvent.ShowGroupAwards) {
                if (uiEvents[i] !is UiEvent.HideGroupAwards) {
                    return false
                }
            } else if (true) {
                TODO()
            }
        }
        TODO()
        return true
    }
}
package com.resolver.scoreboard_management_impl

import com.resolver.scoreboard_management_api.UiEvent

internal fun UiEvent.isImportant() = when (this) {
    is UiEvent.AcceptICPC, is UiEvent.Reject -> true
    else -> false
}
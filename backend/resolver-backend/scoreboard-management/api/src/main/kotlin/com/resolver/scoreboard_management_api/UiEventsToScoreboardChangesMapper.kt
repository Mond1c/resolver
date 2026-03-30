package com.resolver.scoreboard_management_api

import com.resolver.resolution_logic_api.MutableRow
import com.resolver.resolution_logic_api.ScoreboardChanges

interface UiEventsToScoreboardChangesMapper<T : MutableRow> {
    infix fun convert(uiEvent: UiEvent): ScoreboardChanges<T>

    infix fun reverse(uiEvent: UiEvent): ScoreboardChanges<T>
}
package com.resolver.resolver_server_impl

import com.resolver.scoreboard_management_api.UiEvent
import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId

internal fun String.createWsUrlString(host: String, port: Int): String {
    return "ws://$host:$port/$this"
}

internal fun MutableMap<TeamId, ScoreboardRow>.handleUiEvent(
    uiEvent: UiEvent
) {
    if (uiEvent is UiEvent.Accept) {
        put(uiEvent.teamId, uiEvent.row)
    }
    if (uiEvent is UiEvent.Reject) {
        put(uiEvent.teamId, uiEvent.row)
    }
}

internal fun Map<TeamId, ScoreboardRow>.applyUiEvents(
    uiEvents: List<UiEvent>
): Map<TeamId, ScoreboardRow> {
    return buildMap {
        putAll(this@applyUiEvents)
        uiEvents.forEach { this@buildMap.handleUiEvent(it) }
    }
}
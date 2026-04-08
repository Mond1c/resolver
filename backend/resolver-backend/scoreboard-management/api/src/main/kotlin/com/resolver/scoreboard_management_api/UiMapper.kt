package com.resolver.scoreboard_management_api

import com.resolver.resolution_logic_api.ResolutionStep

interface UiMapper {
    infix fun mapToUiEvents(steps: List<ResolutionStep>): List<UiEvent>

    infix fun mapToUiEvent(step: ResolutionStep.WithTeamId.ICPCAcceptResolutionStep): UiEvent

    infix fun mapToUiEvent(step: ResolutionStep.WithTeamId.RejectResolutionStep): UiEvent

    infix fun reverse(uiEvent: UiEvent): UiEvent
}
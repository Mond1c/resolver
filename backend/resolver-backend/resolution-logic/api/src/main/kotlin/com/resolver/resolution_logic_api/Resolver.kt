package com.resolver.resolution_logic_api

import org.icpclive.cds.api.ContestState

interface Resolver {
    fun resolve(
        frozenState: ContestState,
        states: List<ContestState>,
        awardIdToAwardBehaviour: Map<String, AwardBehaviour>
    ): ResolutionResult
}
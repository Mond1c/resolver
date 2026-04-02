package com.resolver.resolution_logic_api

import org.icpclive.cds.api.ContestState

interface Resolver {
    fun resolve(states: List<ContestState>): ResolutionResult
}
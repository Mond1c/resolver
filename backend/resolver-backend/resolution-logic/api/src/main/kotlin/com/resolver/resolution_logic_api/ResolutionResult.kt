package com.resolver.resolution_logic_api

import org.icpclive.cds.api.ContestState

interface ResolutionResult {
    val steps: List<ResolutionStep>
    val contestStateRightBeforeFreeze: ContestState
    val snapshots: List<ContestState>

    operator fun component1() = steps
    operator fun component2() = contestStateRightBeforeFreeze
    operator fun component3() = snapshots
}
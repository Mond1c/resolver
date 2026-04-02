package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.ResolutionResult
import com.resolver.resolution_logic_api.ResolutionStep
import org.icpclive.cds.api.ContestState

internal class ResolutionResultImpl(
    override val steps: List<ResolutionStep>,
    override val contestStateRightBeforeFreeze: ContestState
) : ResolutionResult
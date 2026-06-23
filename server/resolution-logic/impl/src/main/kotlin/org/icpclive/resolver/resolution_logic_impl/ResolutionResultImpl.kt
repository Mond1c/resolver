package org.icpclive.resolver.resolution_logic_impl

import org.icpclive.cds.api.ContestState
import org.icpclive.resolver.resolution_logic_api.ResolutionResult
import org.icpclive.resolver.resolution_logic_api.ResolutionStep

internal class ResolutionResultImpl(
    override val steps: List<ResolutionStep>,
    override val snapshots: List<ContestState>
) : ResolutionResult
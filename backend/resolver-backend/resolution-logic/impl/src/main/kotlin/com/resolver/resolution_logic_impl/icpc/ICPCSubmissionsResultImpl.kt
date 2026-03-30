package com.resolver.resolution_logic_impl.icpc

import com.resolver.resolution_logic_api.ICPCSubmissionsResult
import kotlin.time.Duration

internal class ICPCSubmissionsResultImpl(
    override val isFirstToSolve: Boolean,
    override val penaltyTime: Duration,
    override val isSolved: Boolean,
    override val isPending: Boolean,
    override var isOpened: Boolean
) : ICPCSubmissionsResult
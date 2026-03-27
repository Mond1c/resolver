package com.resolver

import kotlin.time.Duration

data class ICPCSubmissionsResultImpl(
    override val penaltyTime: Duration,
    override val isSolved: Boolean,
    override val isPending: Boolean
) : ICPCSubmissionsResult {
    override var isOpened: Boolean = false
}

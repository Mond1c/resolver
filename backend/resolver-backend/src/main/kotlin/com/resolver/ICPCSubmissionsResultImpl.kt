package com.resolver

import kotlin.time.Duration

data class ICPCSubmissionsResultImpl(
    override val penaltyTime: Duration,
    override val isSolved: Boolean,
    override val isPending: Boolean,
    override var isOpened: Boolean
) : ICPCSubmissionsResult
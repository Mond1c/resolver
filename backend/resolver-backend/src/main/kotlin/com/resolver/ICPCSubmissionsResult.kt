package com.resolver

import kotlin.time.Duration

interface ICPCSubmissionsResult : SubmissionsResult {
    val penaltyTime: Duration
    val isSolved: Boolean
    val isPending: Boolean
}
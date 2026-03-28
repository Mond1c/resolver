package com.resolver

import kotlin.time.Duration

interface ICPCSubmissionsResult : SubmissionsResult {
    val isFirstToSolve: Boolean
    val penaltyTime: Duration
    val isSolved: Boolean
    val isPending: Boolean
}
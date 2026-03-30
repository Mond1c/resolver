package com.resolver.resolution_logic_api

import kotlin.time.Duration

interface ICPCSubmissionsResult : SubmissionsResult {
    val isFirstToSolve: Boolean
    val penaltyTime: Duration
    val isSolved: Boolean
    val isPending: Boolean
}
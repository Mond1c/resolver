package com.resolver

import kotlin.time.Duration

interface MutableICPCRow : MutableRow {
    var totalPenaltyTime: Duration
    var solvedCount: Int
    val problemIdToSubmissionsResult: HashMap<String, ICPCSubmissionsResult>
}
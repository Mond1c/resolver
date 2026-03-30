package com.resolver.resolution_logic_api

import kotlin.time.Duration

interface MutableICPCRow : MutableRow {
    var totalPenaltyTime: Duration
    var solvedCount: Int
    val problemIdToSubmissionsResult: HashMap<String, ICPCSubmissionsResult>

    infix fun isBetterThan(other: MutableICPCRow): Boolean
}
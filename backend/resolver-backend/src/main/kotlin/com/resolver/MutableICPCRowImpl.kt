package com.resolver

import kotlin.time.Duration

class MutableICPCRowImpl(
    override var teamId: String,
    override var rank: Int,
    override var totalPenaltyTime: Duration,
    override var solvedCount: Int,
    override val problemIdToSubmissionsResult: HashMap<String, ICPCSubmissionsResult>,
) : MutableICPCRow

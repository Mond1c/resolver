package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.ICPCSubmissionsResult
import com.resolver.resolution_logic_api.MutableICPCRow
import kotlin.time.Duration

internal data class MutableICPCRowImpl(
    override var teamId: String,
    override var rank: Int,
    override var totalPenaltyTime: Duration,
    override var solvedCount: Int,
    override val problemIdToSubmissionsResult: HashMap<String, ICPCSubmissionsResult>,
) : MutableICPCRow

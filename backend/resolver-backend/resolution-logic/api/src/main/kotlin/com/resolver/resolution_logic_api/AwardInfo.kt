package com.resolver.resolution_logic_api

import kotlinx.serialization.Serializable

@Serializable
data class AwardInfo(
    val awardId: String,
    val behaviour: AwardBehaviour = AwardBehaviour.AFTER_EACH
)
package com.resolver

import kotlinx.serialization.Serializable

@Serializable
data class AwardInfo(
    val awardId: String,
    val behaviour: AwardBehaviour = AwardBehaviour.AfterEach
)
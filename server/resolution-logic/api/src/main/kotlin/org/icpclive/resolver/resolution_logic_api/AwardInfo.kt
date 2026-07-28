package org.icpclive.resolver.resolution_logic_api

import kotlinx.serialization.Serializable

typealias AwardId = String

@Serializable
data class AwardInfo(
    val awardId: AwardId,
    val behaviour: AwardBehaviour = AwardBehaviour.AFTER_EACH
)
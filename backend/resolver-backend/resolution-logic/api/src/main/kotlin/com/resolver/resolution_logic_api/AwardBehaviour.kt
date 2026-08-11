package com.resolver.resolution_logic_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AwardBehaviour {
    @SerialName("ignore")
    IGNORE,
    @SerialName("after_each")
    AFTER_EACH,
    @SerialName("after_all")
    AFTER_ALL,
}
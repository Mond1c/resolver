package com.resolver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AwardBehaviour {
    @Serializable
    @SerialName("ignore")
    object Ignore : AwardBehaviour

    @Serializable
    @SerialName("after each")
    object AfterEach : AwardBehaviour

    @Serializable
    @SerialName("after all")
    object AfterAll : AwardBehaviour
}
package com.resolver.scoreboard_management_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.icpclive.cds.api.TeamId

@Serializable
sealed interface ServerToControllerMessage {
    @Serializable
    @SerialName("VariantsToGoto")
    class VariantsToGoto(
        val teamId: TeamId,
        val fullName: String,
        val variants: List<VariantToGoto>
    ) : ServerToControllerMessage
}

@Serializable
class VariantToGoto(
    val stateIndex: Int,
    val problemsToResolveDisplayNames: List<String>
)

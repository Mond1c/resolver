package com.resolver.util_api

import com.resolver.resolution_logic_api.AwardBehaviour
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

interface AwardsOptionHandler {
    suspend fun handleGenAwardsOption(
        scope: CoroutineScope,
        isGenAwardsOptionEnabled: Boolean,
        awardsBehaviourPath: Path
    )

    fun getAwardIdToBehaviour(awardsBehaviourPath: Path): Map<String, AwardBehaviour>
}
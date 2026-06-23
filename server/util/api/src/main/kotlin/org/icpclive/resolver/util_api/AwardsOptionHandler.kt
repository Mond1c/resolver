package org.icpclive.resolver.util_api

import kotlinx.coroutines.CoroutineScope
import org.icpclive.resolver.resolution_logic_api.AwardBehaviour
import java.nio.file.Path

interface AwardsOptionHandler {
    suspend fun handleGenAwardsOption(
        scope: CoroutineScope,
        isGenAwardsOptionEnabled: Boolean,
        awardsBehaviourPath: Path,
        isAnotherGenNeeded: Boolean
    )

    fun getAwardIdToBehaviour(awardsBehaviourPath: Path): Map<String, AwardBehaviour>
}
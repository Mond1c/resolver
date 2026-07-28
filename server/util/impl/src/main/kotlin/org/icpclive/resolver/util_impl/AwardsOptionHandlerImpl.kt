package org.icpclive.resolver.util_impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import org.icpclive.resolver.resolution_logic_api.AwardInfo
import org.icpclive.resolver.util_api.AwardsOptionHandler
import org.icpclive.resolver.util_api.ContestStatesLoader
import org.icpclive.resolver.util_api.ScoreboardCalculator
import org.icpclive.resolver.util_api.YesNoConsoleHandler
import org.icpclive.resolver.util_api.exception.CoreExceptions
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.exitProcess

class AwardsOptionHandlerImpl(
    private val calculator: ScoreboardCalculator,
    private val contestStatesLoader: ContestStatesLoader,
    private val yesNoConsoleHandler: YesNoConsoleHandler,
    private val json: Json
) : AwardsOptionHandler {
    override suspend fun handleGenAwardsOption(
        scope: CoroutineScope,
        isGenAwardsOptionEnabled: Boolean,
        awardsBehaviourPath: Path,
        isAnotherGenNeeded: Boolean
    ) {
        if (isGenAwardsOptionEnabled) {
            val yes = if (awardsBehaviourPath.exists()) {
                println("Template already exists. Are you sure you want to regenerate awards behaviour template? [y/n]")
                yesNoConsoleHandler.handleYesNo()
            } else {
                false
            }
            if (yes || !awardsBehaviourPath.exists()) {
                val awardsSemaphore = Semaphore(1, 1)
                val dst = mutableListOf<ContestState>()
                val awardsJob = contestStatesLoader.loadContestStates(
                    scope = scope,
                    dst = dst,
                    semaphore = awardsSemaphore,
                    submissionResultsAfterFreezeInput = true
                )
                awardsSemaphore.acquire()
                awardsJob.cancel()
                val calculations =
                    calculator.calculateScoreboard(
                        dst.lastOrNull() ?: throw CoreExceptions.contestStatesIsEmptyException
                    )
                val awards =
                    calculations?.ranks?.awards ?: throw CoreExceptions.contestInfoIsNullException
                awardsBehaviourPath.writeText(
                    json.encodeToString<List<AwardInfo>>(awards.map {
                        AwardInfo(
                            awardId = it.id
                        )
                    })
                )
            }
            if (!isAnotherGenNeeded) {
                exitProcess(0)
            }
        }
    }

    override fun getAwardIdToBehaviour(awardsBehaviourPath: Path) = try {
        awardsBehaviourPath
            .readText()
            .let { json.decodeFromString<List<AwardInfo>>(it) }
            .associateBy { it.awardId }
            .mapValues { it.value.behaviour }
    } catch (_: Exception) {
        hashMapOf()
    }
}
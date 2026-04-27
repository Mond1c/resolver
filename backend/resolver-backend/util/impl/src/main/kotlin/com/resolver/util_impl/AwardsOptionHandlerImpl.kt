package com.resolver.util_impl

import com.resolver.resolution_logic_api.AwardInfo
import com.resolver.util_api.AwardsOptionHandler
import com.resolver.util_api.ContestStatesLoader
import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_api.YesNoConsoleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
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
                    calculator.calculateScoreboard(dst.lastOrNull() ?: TODO("Handle this case gracefully"))
                val awards = calculations?.ranks?.awards ?: TODO("Unexpected null")
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
            .groupBy { it.awardId }
            .mapValues { it.value.firstOrNull()?.behaviour ?: TODO("Unexpected null") }
    } catch (_: Exception) {
        hashMapOf()
    }
}
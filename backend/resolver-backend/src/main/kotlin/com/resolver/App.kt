package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_api.AwardInfo
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.resolver_server_api.StartResult
import com.resolver.resolver_server_di.ResolverServerComponent
import com.resolver.scoreboard_management_di.ScoreboardManagementComponent
import com.resolver.util_di.ResolverUtilComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.icpclive.cds.CommentaryMessagesUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.ContestResultType
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ContestStatus
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.exitProcess

object App : CliktCommand() {
    private val resolverOptions by ResolverCommandLineOptions()
    private val awardsBehaviourPath: Path
        get() = resolverOptions.configDirectory.resolve("awards_behaviour.json")

    override fun run() {
        runBlocking {
            handleGenAwardsOption()

            val frozen = mutableListOf<ContestState>()
            val notFrozen = mutableListOf<ContestState>()
            val semaphore = Semaphore(2, 2)

            val awardIdToBehaviour = getAwardIdToBehaviour()

            val frozenJob = launch {
                loadContestStates(
                    dst = frozen,
                    semaphore = semaphore,
                    submissionResultsAfterFreezeInput = false
                )
            }

            val notFrozenJob = launch {
                loadContestStates(
                    dst = notFrozen,
                    semaphore = semaphore,
                    submissionResultsAfterFreezeInput = true
                )
            }

            semaphore.acquire()
            semaphore.acquire()

            frozenJob.cancel()
            notFrozenJob.cancel()

            val frozenState = frozen.lastOrNull() ?: TODO("Handle this case gracefully")
            val resolver = when (frozenState.infoAfterEvent?.resultType) {
                ContestResultType.ICPC -> {
                    ResolutionLogicComponent.greedyICPCResolver
                }

                ContestResultType.IOI -> {
                    ResolutionLogicComponent.greedyIOIResolver
                }

                else -> {
                    TODO("Unsupported contest result type")
                }
            }
            val result = resolver.resolve(notFrozen, awardIdToBehaviour)
            val manager = ScoreboardManagementComponent.provideScoreboardManager1(
                frozenState = frozenState,
                snapshots = result.snapshots,
                steps = result.steps
            )
            val server = ResolverServerComponent.provideServer1(
                manager,
                ScoreboardManagementComponent.json
            )
            when (val startResult = server.start(resolverOptions.port, resolverOptions.host)) {
                StartResult.AlreadyStarted -> {}
                StartResult.Failure -> {}
                is StartResult.MaybeSuccess -> {
                    startResult.startJob.join()
                }
            }
        }
    }

    private suspend fun loadContestStates(
        dst: MutableList<ContestState>,
        semaphore: Semaphore,
        submissionResultsAfterFreezeInput: Boolean
    ) {
        resolverOptions.toFlow().addComputedData {
            firstToSolves = true
            submissionResultsAfterFreeze = submissionResultsAfterFreezeInput
            autoFinalize = true
        }
            .contestState()
            .collect { state ->
                when (val lastEvent = state.lastEvent) {
                    is CommentaryMessagesUpdate -> {}
                    is InfoUpdate -> {
                        if (lastEvent.newInfo.status is ContestStatus.OVER) {
                            dst.add(state)
                            semaphore.release()
                        }
                    }

                    is RunUpdate -> {
                        dst.add(state)
                    }
                }
            }
    }

    private suspend fun CoroutineScope.handleGenAwardsOption() {
        if (resolverOptions.genAwards) {
            var yes = false
            if (awardsBehaviourPath.exists()) {
                println("Template already exists. Are you sure you want to regenerate awards behaviour template? [y/n]")
                while (true) {
                    when (readln().trim().lowercase()) {
                        "y", "yes" -> {
                            yes = true
                            break
                        }

                        "n", "no" -> {
                            break
                        }

                        else -> {}
                    }
                }
            }
            if (yes || !awardsBehaviourPath.exists()) {
                val awardsSemaphore = Semaphore(1, 1)
                val dst = mutableListOf<ContestState>()
                val awardsJob = launch {
                    loadContestStates(
                        dst = dst,
                        semaphore = awardsSemaphore,
                        submissionResultsAfterFreezeInput = true
                    )
                }
                awardsSemaphore.acquire()
                awardsJob.cancel()
                val calculator = ResolverUtilComponent.scoreboardCalculator1
                val calculations =
                    calculator.calculateScoreboard(dst.lastOrNull() ?: TODO("Handle this case gracefully"))
                val awards = calculations?.ranks?.awards ?: TODO("Unexpected null")
                awardsBehaviourPath.writeText(
                    ScoreboardManagementComponent.json.encodeToString(awards.map {
                        AwardInfo(
                            awardId = it.id
                        )
                    })
                )
            }
            exitProcess(0)
        }
    }

    private fun getAwardIdToBehaviour() = try {
        awardsBehaviourPath
            .readText()
            .let { ScoreboardManagementComponent.json.decodeFromString<List<AwardInfo>>(it) }
            .groupBy { it.awardId }
            .mapValues { it.value.firstOrNull()?.behaviour ?: TODO("Unexpected null") }
    } catch (_: Exception) {
        hashMapOf()
    }
}
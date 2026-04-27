package com.resolver.util_impl

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.StartResult
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.util_api.App
import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_api.YesNoConsoleHandler
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import java.nio.file.Path

class AppImpl(
    private val calculator: ScoreboardCalculator,
    private val yesNoConsoleHandler: YesNoConsoleHandler,
    private val json: Json,
    private val createScoreboardManager: (ContestState, List<ContestState>, List<ResolutionStep>) -> ScoreboardManager,
    private val createServer: (ScoreboardManager, Json) -> Server,
    private val chooseResolver: (ContestState) -> Resolver
) : App() {
    private val resolverOptions by ResolverCommandLineOptionsImpl()
    private val awardsBehaviourPath: Path
        get() = resolverOptions.configDirectory.resolve("awards_behaviour.json")

    override fun run() {
        val contestStatesLoader = ContestStatesLoaderImpl(resolverOptions)
        val awardsOptionHandler = AwardsOptionHandlerImpl(
            calculator = calculator,
            contestStatesLoader = contestStatesLoader,
            yesNoConsoleHandler = yesNoConsoleHandler,
            json = json
        )
        runBlocking {
            awardsOptionHandler.handleGenAwardsOption(
                scope = this,
                isGenAwardsOptionEnabled = resolverOptions.genAwards,
                awardsBehaviourPath = awardsBehaviourPath
            )

            val frozen = mutableListOf<ContestState>()
            val notFrozen = mutableListOf<ContestState>()
            val semaphore = Semaphore(2, 2)

            val awardIdToBehaviour = awardsOptionHandler.getAwardIdToBehaviour(
                awardsBehaviourPath = awardsBehaviourPath
            )

            val frozenJob = contestStatesLoader.loadContestStates(
                this,
                dst = frozen,
                semaphore = semaphore,
                submissionResultsAfterFreezeInput = false
            )

            val notFrozenJob = contestStatesLoader.loadContestStates(
                scope = this,
                dst = notFrozen,
                semaphore = semaphore,
                submissionResultsAfterFreezeInput = true
            )

            semaphore.acquire()
            semaphore.acquire()

            frozenJob.cancel()
            notFrozenJob.cancel()

            val frozenState = frozen.lastOrNull() ?: TODO("Handle this case gracefully")
            val resolver = chooseResolver(frozenState)
            val result = resolver.resolve(frozenState, notFrozen, awardIdToBehaviour)
            val manager = createScoreboardManager(
                frozenState,
                result.snapshots,
                result.steps
            )
            val server = createServer(
                manager,
                json
            )
            when (val startResult = server.start(resolverOptions.extractStartServerOptions())) {
                StartResult.AlreadyStarted -> {}
                StartResult.Failure -> {}
                is StartResult.MaybeSuccess -> {
                    startResult.startJob.join()
                }
            }
        }
    }
}
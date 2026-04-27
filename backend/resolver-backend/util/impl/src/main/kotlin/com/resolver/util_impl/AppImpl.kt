package com.resolver.util_impl

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.ServerOptions
import com.resolver.resolver_server_api.StartResult
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.ScoreboardManagerOptions
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
    private val createScoreboardManager: (
        ContestState, List<ContestState>, List<ResolutionStep>, ScoreboardManagerOptions
    ) -> ScoreboardManager,
    private val createServer: (ScoreboardManager, Json, ServerOptions) -> Server,
    private val chooseResolver: (ContestState) -> Resolver
) : App() {
    private val resolverOptions by ResolverCommandLineOptionsImpl()
    private val awardsBehaviourPath: Path
        get() = resolverOptions.configDirectory.resolve("awards_behaviour.json")
    private val resolverOptionsPath: Path
        get() = resolverOptions.configDirectory.resolve("resolver.json")

    override fun run() {
        val contestStatesLoader = ContestStatesLoaderImpl(resolverOptions)
        val awardsOptionHandler = AwardsOptionHandlerImpl(
            calculator = calculator,
            contestStatesLoader = contestStatesLoader,
            yesNoConsoleHandler = yesNoConsoleHandler,
            json = json
        )
        val resolverOptionsHandler = ResolverOptionsHandlerImpl(
            json = json,
            yesNoConsoleHandler = yesNoConsoleHandler
        )
        runBlocking {
            awardsOptionHandler.handleGenAwardsOption(
                scope = this,
                isGenAwardsOptionEnabled = resolverOptions.genAwards,
                awardsBehaviourPath = awardsBehaviourPath,
                isAnotherGenNeeded = resolverOptions.genResolverOptions
            )

            resolverOptionsHandler.handleGenResolverOptionsOption(
                isGenResolverOptionsOptionEnabled = resolverOptions.genResolverOptions,
                resolverOptionsPath = resolverOptionsPath
            )

            val frozen = mutableListOf<ContestState>()
            val notFrozen = mutableListOf<ContestState>()
            val semaphore = Semaphore(2, 2)

            val awardIdToBehaviour = awardsOptionHandler.getAwardIdToBehaviour(
                awardsBehaviourPath = awardsBehaviourPath
            )

            val merged = resolverOptionsHandler.getResolverOptions(
                resolverOptionsPath = resolverOptionsPath
            ).mergeWithCommandLineOptions(resolverOptions)

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
                result.steps,
                merged.extractScoreboardManagerOptions()
            )
            val server = createServer(
                manager,
                json,
                merged.extractServerOptions()
            )
            when (val startResult = server.start(merged.extractStartServerOptions())) {
                StartResult.AlreadyStarted -> {}
                StartResult.Failure -> {}
                is StartResult.MaybeSuccess -> {
                    startResult.startJob.join()
                }
            }
        }
    }
}
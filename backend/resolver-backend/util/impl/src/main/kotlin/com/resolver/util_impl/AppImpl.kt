package com.resolver.util_impl

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.ServerOptions
import com.resolver.resolver_server_api.StartResult
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.ScoreboardManagerOptions
import com.resolver.util_api.Passwords
import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_api.YesNoConsoleHandler
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState

class AppImpl(
    calculator: ScoreboardCalculator,
    yesNoConsoleHandler: YesNoConsoleHandler,
    private val json: Json,
    private val createScoreboardManager: (
        ContestState, List<ContestState>, List<ResolutionStep>, ScoreboardManagerOptions
    ) -> ScoreboardManager,
    private val createServer: (ScoreboardManager, Json, Passwords, ServerOptions) -> Server,
    private val chooseResolver: (ContestState) -> Resolver
) : AppBase(
    calculator,
    yesNoConsoleHandler,
    json,
    { passwords, awardIdToBehaviour, resolverOptions, frozen, notFrozen ->
        val frozenState = frozen.lastOrNull() ?: TODO("Handle this case gracefully")
        val resolver = chooseResolver(frozenState)
        val result = resolver.resolve(frozenState, notFrozen, awardIdToBehaviour)
        val manager = createScoreboardManager(
            frozenState,
            result.snapshots,
            result.steps,
            resolverOptions.extractScoreboardManagerOptions()
        )
        val server = createServer(
            manager,
            json,
            passwords,
            resolverOptions.extractServerOptions()
        )
        when (val startResult = server.start(resolverOptions.extractStartServerOptions())) {
            StartResult.AlreadyStarted -> {}
            StartResult.Failure -> {}
            is StartResult.MaybeSuccess -> {
                startResult.startJob.join()
            }
        }
    })
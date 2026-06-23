package org.icpclive.resolver.util_impl

import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import org.icpclive.resolver.resolution_logic_api.ResolutionStep
import org.icpclive.resolver.resolution_logic_api.Resolver
import org.icpclive.resolver.resolver_server_api.Server
import org.icpclive.resolver.resolver_server_api.ServerOptions
import org.icpclive.resolver.resolver_server_api.StartResult
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManagerOptions
import org.icpclive.resolver.util_api.ResolverAccounts
import org.icpclive.resolver.util_api.ScoreboardCalculator
import org.icpclive.resolver.util_api.YesNoConsoleHandler
import org.icpclive.resolver.util_api.exception.CoreExceptions

class AppImpl(
    calculator: ScoreboardCalculator,
    yesNoConsoleHandler: YesNoConsoleHandler,
    private val json: Json,
    private val createScoreboardManager: (
        ContestState, List<ContestState>, List<ResolutionStep>, ScoreboardManagerOptions
    ) -> ScoreboardManager,
    private val createServer: (ScoreboardManager, Json, ResolverAccounts, ServerOptions) -> Server,
    private val chooseResolver: (ContestState) -> Resolver
) : AppBase(
    calculator,
    yesNoConsoleHandler,
    json,
    { passwords, awardIdToBehaviour, resolverOptions, frozen, notFrozen ->
        val frozenState = frozen.lastOrNull() ?: throw CoreExceptions.contestStatesIsEmptyException
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
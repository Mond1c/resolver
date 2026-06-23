package org.icpclive.resolver.util_di

import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import org.icpclive.resolver.resolution_logic_api.AwardBehaviour
import org.icpclive.resolver.resolution_logic_api.ResolutionStep
import org.icpclive.resolver.resolution_logic_api.Resolver
import org.icpclive.resolver.resolver_server_api.Server
import org.icpclive.resolver.resolver_server_api.ServerOptions
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManagerOptions
import org.icpclive.resolver.util_api.App
import org.icpclive.resolver.util_api.ResolverAccounts
import org.icpclive.resolver.util_api.ResolverOptions
import org.icpclive.resolver.util_api.ScoreboardCalculator
import org.icpclive.resolver.util_api.YesNoConsoleHandler
import org.icpclive.resolver.util_impl.AppBase
import org.icpclive.resolver.util_impl.AppImpl
import org.icpclive.resolver.util_impl.ScoreboardCalculatorImpl
import org.icpclive.resolver.util_impl.YesNoConsoleHandlerImpl

object ResolverUtilComponent {
    val scoreboardCalculator: ScoreboardCalculator = ScoreboardCalculatorImpl

    val yesNoConsoleHandler: YesNoConsoleHandler = YesNoConsoleHandlerImpl

    fun provideApp(
        json: Json,
        createScoreboardManager: (
            ContestState, List<ContestState>, List<ResolutionStep>, ScoreboardManagerOptions
        ) -> ScoreboardManager,
        createServer: (ScoreboardManager, Json, ResolverAccounts, ServerOptions) -> Server,
        chooseResolver: (ContestState) -> Resolver
    ): App {
        return AppImpl(
            calculator = scoreboardCalculator,
            yesNoConsoleHandler = yesNoConsoleHandler,
            json = json,
            createScoreboardManager = createScoreboardManager,
            createServer = createServer,
            chooseResolver = chooseResolver
        )
    }

    fun provideAppBase(
        calculator: ScoreboardCalculator,
        yesNoConsoleHandler: YesNoConsoleHandler,
        json: Json,
        block: suspend (
            ResolverAccounts,
            Map<String, AwardBehaviour>,
            ResolverOptions,
            frozen: List<ContestState>,
            notFrozen: List<ContestState>
        ) -> Unit
    ): App {
        return AppBase(calculator, yesNoConsoleHandler, json, block)
    }

}
package com.resolver.util_di

import com.resolver.resolution_logic_api.AwardBehaviour
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.ServerOptions
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.ScoreboardManagerOptions
import com.resolver.util_api.App
import com.resolver.util_api.ResolverAccounts
import com.resolver.util_api.ResolverOptions
import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_api.YesNoConsoleHandler
import com.resolver.util_impl.AppBase
import com.resolver.util_impl.AppImpl
import com.resolver.util_impl.ScoreboardCalculatorImpl
import com.resolver.util_impl.YesNoConsoleHandlerImpl
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState

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
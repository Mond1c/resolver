package com.resolver.util_di

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolver_server_api.Server
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.util_api.*
import com.resolver.util_impl.*
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.cli.CdsCommandLineOptions

object ResolverUtilComponent {
    val scoreboardCalculator1: ScoreboardCalculator by lazy {
        ScoreboardCalculatorImpl
    }

    val yesNoConsoleHandler: YesNoConsoleHandler by lazy {
        YesNoConsoleHandlerImpl
    }

    val resolverCommandLineOptions: ResolverCommandLineOptions by lazy {
        ResolverCommandLineOptionsImpl()
    }

    fun provideContestStatesLoader(
        cdsCommandLineOptions: CdsCommandLineOptions
    ): ContestStatesLoader {
        return ContestStatesLoaderImpl(
            cdsCommandLineOptions = cdsCommandLineOptions
        )
    }

    fun provideAwardsOptionHandler(
        calculator: ScoreboardCalculator,
        contestStatesLoader: ContestStatesLoader,
        yesNoConsoleHandler: YesNoConsoleHandler,
        json: Json
    ): AwardsOptionHandler {
        return AwardsOptionHandlerImpl(
            calculator = calculator,
            contestStatesLoader = contestStatesLoader,
            yesNoConsoleHandler = yesNoConsoleHandler,
            json = json
        )
    }

    fun provideApp(
        json: Json,
        createScoreboardManager: (ContestState, List<ContestState>, List<ResolutionStep>) -> ScoreboardManager,
        createServer: (ScoreboardManager, Json) -> Server,
        chooseResolver: (ContestState) -> Resolver
    ): App {
        return AppImpl(
            calculator = scoreboardCalculator1,
            yesNoConsoleHandler = yesNoConsoleHandler,
            json = json,
            createScoreboardManager = createScoreboardManager,
            createServer = createServer,
            chooseResolver = chooseResolver
        )
    }

}
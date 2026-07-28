package org.icpclive.resolver.scoreboard_management_di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState
import org.icpclive.resolver.resolution_logic_api.ResolutionStep
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManagerOptions
import org.icpclive.resolver.scoreboard_management_api.UiMapper
import org.icpclive.resolver.scoreboard_management_impl.ScoreboardManagerImpl
import org.icpclive.resolver.scoreboard_management_impl.UiMapperImpl
import org.icpclive.resolver.util_di.ResolverUtilComponent

object ScoreboardManagementComponent {
    val uiMapper: UiMapper by lazy {
        UiMapperImpl
    }

    val json: Json by lazy {
        Json {
            prettyPrint = true
            encodeDefaults = true
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun provideScoreboardManager(
        frozenState: ContestState,
        snapshots: List<ContestState>,
        steps: List<ResolutionStep>,
        scoreboardManagerOptions: ScoreboardManagerOptions
    ): ScoreboardManager {
        return ScoreboardManagerImpl(
            frozenState = frozenState,
            snapshots = snapshots,
            calculator = ResolverUtilComponent.scoreboardCalculator,
            steps = steps,
            uiMapper = uiMapper,
            scoreboardManagerOptions = scoreboardManagerOptions
        )
    }
}
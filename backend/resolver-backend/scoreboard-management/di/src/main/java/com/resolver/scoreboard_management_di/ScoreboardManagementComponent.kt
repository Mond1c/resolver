package com.resolver.scoreboard_management_di

import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.UiMapper
import com.resolver.scoreboard_management_impl.ScoreboardManagerImpl
import com.resolver.scoreboard_management_impl.UiMapperImpl
import com.resolver.util_di.ResolverUtilComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.ContestState

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
    fun provideScoreboardManager1(
        frozenState: ContestState,
        snapshots: List<ContestState>,
        steps: List<ResolutionStep>
    ): ScoreboardManager {
        return ScoreboardManagerImpl(
            frozenState = frozenState,
            snapshots = snapshots,
            calculator = ResolverUtilComponent.scoreboardCalculator1,
            steps = steps,
            uiMapper = uiMapper,
        )
    }
}
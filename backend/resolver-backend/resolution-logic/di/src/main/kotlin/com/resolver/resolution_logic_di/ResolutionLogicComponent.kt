package com.resolver.resolution_logic_di

import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolution_logic_impl.AwardsHandlerImpl
import com.resolver.resolution_logic_impl.GreedyICPCResolver
import com.resolver.resolution_logic_impl.GreedyIOIResolver
import com.resolver.util_di.ResolverUtilComponent
import org.icpclive.cds.api.ContestResultType
import org.icpclive.cds.api.ContestState

object ResolutionLogicComponent {
    val greedyICPCResolver: Resolver by lazy {
        GreedyICPCResolver(
            ResolverUtilComponent.scoreboardCalculator1,
            AwardsHandlerImpl
        )
    }

    val greedyIOIResolver: Resolver by lazy {
        GreedyIOIResolver(
            ResolverUtilComponent.scoreboardCalculator1,
            AwardsHandlerImpl
        )
    }

    fun provideResolver(state: ContestState): Resolver = when (state.infoAfterEvent?.resultType ?: TODO()) {
        ContestResultType.ICPC -> {
            greedyICPCResolver
        }

        ContestResultType.IOI -> {
            greedyIOIResolver
        }
    }
}
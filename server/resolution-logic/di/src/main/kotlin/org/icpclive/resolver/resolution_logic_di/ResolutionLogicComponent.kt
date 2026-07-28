package org.icpclive.resolver.resolution_logic_di

import org.icpclive.cds.api.ContestResultType
import org.icpclive.cds.api.ContestState
import org.icpclive.resolver.resolution_logic_api.Resolver
import org.icpclive.resolver.resolution_logic_impl.AwardsHandlerImpl
import org.icpclive.resolver.resolution_logic_impl.GreedyICPCResolver
import org.icpclive.resolver.resolution_logic_impl.GreedyIOIResolver
import org.icpclive.resolver.util_api.exception.CoreExceptions
import org.icpclive.resolver.util_di.ResolverUtilComponent

object ResolutionLogicComponent {
    val greedyICPCResolver: Resolver by lazy {
        GreedyICPCResolver(
            ResolverUtilComponent.scoreboardCalculator,
            AwardsHandlerImpl
        )
    }

    val greedyIOIResolver: Resolver by lazy {
        GreedyIOIResolver(
            ResolverUtilComponent.scoreboardCalculator,
            AwardsHandlerImpl
        )
    }

    fun provideResolver(state: ContestState): Resolver =
        when (state.infoAfterEvent?.resultType ?: throw CoreExceptions.contestInfoIsNullException) {
            ContestResultType.ICPC -> {
                greedyICPCResolver
            }

            ContestResultType.IOI -> {
                greedyIOIResolver
            }
        }
}
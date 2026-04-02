package com.resolver.resolution_logic_di

import com.resolver.resolution_logic_api.Resolver
import com.resolver.resolution_logic_impl.GreedyICPCResolver
import com.resolver.util_di.ResolverUtilComponent

object ResolutionLogicComponent {
    val greedyICPCResolver: Resolver by lazy {
        GreedyICPCResolver(ResolverUtilComponent.scoreboardCalculator1)
    }
}
package com.resolver

import com.github.ajalt.clikt.core.main
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.resolver_server_di.ResolverServerComponent
import com.resolver.scoreboard_management_di.ScoreboardManagementComponent
import com.resolver.util_di.ResolverUtilComponent

fun main(args: Array<String>) = ResolverUtilComponent.provideApp(
    json = ScoreboardManagementComponent.json,
    createScoreboardManager = ScoreboardManagementComponent::provideScoreboardManager,
    createServer = ResolverServerComponent::provideServer,
    chooseResolver = ResolutionLogicComponent::provideResolver
).main(args)
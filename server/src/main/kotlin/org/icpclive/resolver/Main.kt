package org.icpclive.resolver

import com.github.ajalt.clikt.core.main
import org.icpclive.resolver.resolution_logic_di.ResolutionLogicComponent
import org.icpclive.resolver.resolver_server_di.ResolverServerComponent
import org.icpclive.resolver.scoreboard_management_di.ScoreboardManagementComponent
import org.icpclive.resolver.util_di.ResolverUtilComponent

fun main(args: Array<String>) = ResolverUtilComponent.provideApp(
    json = ScoreboardManagementComponent.json,
    createScoreboardManager = ScoreboardManagementComponent::provideScoreboardManager,
    createServer = ResolverServerComponent::provideServer,
    chooseResolver = ResolutionLogicComponent::provideResolver
).main(args)
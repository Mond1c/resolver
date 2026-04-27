package com.resolver

import com.github.ajalt.clikt.core.main
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.resolver_server_di.ResolverServerComponent
import com.resolver.scoreboard_management_di.ScoreboardManagementComponent
import com.resolver.util_di.ResolverUtilComponent

fun main(args: Array<String>) = ResolverUtilComponent.provideApp(
    json = ScoreboardManagementComponent.json,
    createScoreboardManager = { frozenState, snapshots, steps, scoreboardManagerOptions ->
        ScoreboardManagementComponent.provideScoreboardManager1(
            frozenState = frozenState,
            snapshots = snapshots,
            steps = steps,
            scoreboardManagerOptions = scoreboardManagerOptions
        )
    },
    createServer = { scoreboardManager, json, serverOptions ->
        ResolverServerComponent.provideServer1(scoreboardManager, json, serverOptions)
    },
    chooseResolver = { contestState ->
        ResolutionLogicComponent.provideResolver(contestState)
    }
).main(args)
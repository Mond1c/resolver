package org.icpclive.resolver.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import org.icpclive.resolver.controller.ControllerViewModelFactory
import org.icpclive.resolver.scoreboard.ScoreboardViewModelFactory

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val scoreboardViewModelFactory: Lazy<ScoreboardViewModelFactory>
    val controllerViewModelFactory: Lazy<ControllerViewModelFactory>
}
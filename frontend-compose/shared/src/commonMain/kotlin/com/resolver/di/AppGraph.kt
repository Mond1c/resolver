package com.resolver.di

import com.resolver.controller.ControllerViewModelFactory
import com.resolver.scoreboard.ScoreboardViewModelFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
internal interface AppGraph {
    val scoreboardViewModelFactory: Lazy<ScoreboardViewModelFactory>
    val controllerViewModelFactory: Lazy<ControllerViewModelFactory>
}
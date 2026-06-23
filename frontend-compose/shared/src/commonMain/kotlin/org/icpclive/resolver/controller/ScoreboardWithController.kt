package org.icpclive.resolver.controller

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.createGraph
import org.icpclive.resolver.di.AppGraph
import org.icpclive.resolver.scoreboard.Scoreboard

@Composable
internal fun ScoreboardWithController() {
    val graph = remember { createGraph<AppGraph>() }
    Column(Modifier.fillMaxSize()) {
        Scoreboard(graph, Modifier.weight(3f))
        Controller(graph, Modifier.weight(1f))
    }
}
package org.icpclive.resolver

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.icpclive.resolver.controller.ScoreboardWithController
import org.icpclive.resolver.scoreboard.Scoreboard

@Composable
fun ResolverControlApp() {
    MaterialTheme {
        ScoreboardWithController()
    }
}

@Composable
fun ResolverApp() {
    MaterialTheme {
        Scoreboard()
    }
}
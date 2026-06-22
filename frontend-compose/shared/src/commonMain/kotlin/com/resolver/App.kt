package com.resolver

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.resolver.controller.ScoreboardWithController
import com.resolver.scoreboard.Scoreboard

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
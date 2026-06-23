package org.icpclive.resolver.util_api

import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId
import org.icpclive.cds.scoreboard.Ranking

interface ScoreboardCalculations {
    val rows: Map<TeamId, ScoreboardRow>
    val ranks: Ranking

    operator fun component1() = rows
    operator fun component2() = ranks
}
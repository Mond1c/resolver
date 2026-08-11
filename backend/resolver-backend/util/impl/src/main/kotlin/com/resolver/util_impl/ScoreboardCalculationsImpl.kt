package com.resolver.util_impl

import com.resolver.util_api.ScoreboardCalculations
import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId
import org.icpclive.cds.scoreboard.Ranking

internal class ScoreboardCalculationsImpl(
    override val rows: Map<TeamId, ScoreboardRow>,
    override val ranks: Ranking
) : ScoreboardCalculations
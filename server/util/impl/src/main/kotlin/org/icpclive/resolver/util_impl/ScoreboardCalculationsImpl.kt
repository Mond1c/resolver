package org.icpclive.resolver.util_impl

import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId
import org.icpclive.cds.scoreboard.Ranking
import org.icpclive.resolver.util_api.ScoreboardCalculations

internal class ScoreboardCalculationsImpl(
    override val rows: Map<TeamId, ScoreboardRow>,
    override val ranks: Ranking
) : ScoreboardCalculations
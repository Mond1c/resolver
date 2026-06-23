package org.icpclive.resolver.util_impl

import kotlinx.collections.immutable.PersistentMap
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.OptimismLevel
import org.icpclive.cds.api.RunId
import org.icpclive.cds.api.RunInfo
import org.icpclive.cds.scoreboard.getScoreboardCalculator
import org.icpclive.resolver.util_api.ScoreboardCalculations
import org.icpclive.resolver.util_api.ScoreboardCalculator

object ScoreboardCalculatorImpl : ScoreboardCalculator {
    override fun calculateScoreboard(contestState: ContestState): ScoreboardCalculations? {
        return calculateScoreboard(contestState.infoAfterEvent, contestState.runsAfterEvent)
    }

    override fun calculateScoreboard(
        contestInfo: ContestInfo?,
        runs: PersistentMap<RunId, RunInfo>
    ): ScoreboardCalculations? {
        val contestInfo = contestInfo ?: return null
        val calculator = getScoreboardCalculator(contestInfo, OptimismLevel.NORMAL)
        val rows = runs.values.groupBy { runInfo ->
            runInfo.teamId
        }.mapValues { runInfoEntries ->
            calculator.getScoreboardRow(contestInfo, runInfoEntries.value)
        }.toMutableMap()
        rows.putAll(
            (contestInfo.teams.keys.toHashSet() - rows.keys.toHashSet())
                .map { teamId ->
                    Pair(
                        teamId,
                        calculator.getScoreboardRow(contestInfo, listOf())
                    )
                })
        val ranks = calculator.getRanking(contestInfo, rows)
        return ScoreboardCalculationsImpl(rows, ranks)
    }
}
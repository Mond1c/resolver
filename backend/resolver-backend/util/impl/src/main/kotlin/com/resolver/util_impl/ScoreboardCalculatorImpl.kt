package com.resolver.util_impl

import com.resolver.util_api.ScoreboardCalculations
import com.resolver.util_api.ScoreboardCalculator
import kotlinx.collections.immutable.PersistentMap
import org.icpclive.cds.api.*
import org.icpclive.cds.scoreboard.getScoreboardCalculator

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
        }
        val ranks = calculator.getRanking(contestInfo, rows)
        return ScoreboardCalculationsImpl(rows, ranks)
    }
}
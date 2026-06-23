package org.icpclive.resolver.util_api

import kotlinx.collections.immutable.PersistentMap
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.RunId
import org.icpclive.cds.api.RunInfo

interface ScoreboardCalculator {
    fun calculateScoreboard(contestState: ContestState): ScoreboardCalculations?

    fun calculateScoreboard(
        contestInfo: ContestInfo?,
        runs: PersistentMap<RunId, RunInfo>
    ): ScoreboardCalculations?
}
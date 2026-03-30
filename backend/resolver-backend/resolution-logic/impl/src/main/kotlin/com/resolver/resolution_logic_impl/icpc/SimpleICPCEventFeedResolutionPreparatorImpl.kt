package com.resolver.resolution_logic_impl.icpc

import com.resolver.resolution_logic_api.*
import com.resolver.resolution_logic_impl.exceptions.UnexpectedStateException
import java.nio.file.Path

internal class SimpleICPCEventFeedResolutionPreparatorImpl(
    private val eventFeedAnalyzer: EventFeedAnalyzer,
    private val mutableICPCScoreboardBuilder: MutableScoreboardBuilder<MutableICPCRow>,
    eventFeedPath: Path
) : EventFeedResolutionPreparator() {
    init {
        eventFeedAnalyzer.resetAndAnalyze(eventFeedPath)
        eventFeedAnalyzer.filterHiddenTeams()
        eventFeedAnalyzer.filterUnjudgedSubmissions()
    }

    override fun prepareResolution(): List<ResolutionStep> {
        val scoreboard = mutableICPCScoreboardBuilder.build(eventFeedAnalyzer)
        while (true) {
            val row = scoreboard.getCurrentRow() ?: break
            var problemId: String? = null
            for (problemSubmissionsResult in row.problemIdToSubmissionsResult) {
                if (!problemSubmissionsResult.value.isOpened) {
                    problemId = problemSubmissionsResult.key
                    problemSubmissionsResult.value.isOpened = true
                    if (problemSubmissionsResult.value.isSolved) {
                        row.solvedCount++
                        row.totalPenaltyTime += problemSubmissionsResult.value.penaltyTime
                    }
                    break
                }
            }
            if (problemId != null) {
                val step =
                    scoreboard.sort(problemId)
                        ?: throw UnexpectedStateException("Incorrect using of MutableScoreboard")
                steps.add(step)
            } else {
                // TODO: awards
                scoreboard.up()
            }
        }
        return steps
    }
}
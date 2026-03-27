package com.resolver

import java.nio.file.Path

class SimpleICPCEventFeedResolutionPreparatorImpl(
    private val eventFeedAnalyzer: EventFeedAnalyzer,
    private val mutableICPCScoreboardBuilder: MutableScoreboardBuilder<MutableICPCRow>,
    eventFeedPath: Path
) : EventFeedResolutionPreparator() {
    init {
        eventFeedAnalyzer.resetAndAnalyze(eventFeedPath)
    }

    override fun prepareResolution() {
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
                steps.add(scoreboard.sort(problemId))
            } else {
                // TODO: awards
                scoreboard.up()
            }
        }
    }
}
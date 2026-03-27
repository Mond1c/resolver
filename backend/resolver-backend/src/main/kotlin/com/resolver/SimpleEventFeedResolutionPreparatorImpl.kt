package com.resolver

import java.nio.file.Path

class SimpleEventFeedResolutionPreparatorImpl(
    private val eventFeedAnalyzer: EventFeedAnalyzer,
    eventFeedPath: Path
) : EventFeedResolutionPreparator() {
    init {
        eventFeedAnalyzer.resetAndAnalyze(eventFeedPath)
    }

    override fun prepareResolution() {

    }
}
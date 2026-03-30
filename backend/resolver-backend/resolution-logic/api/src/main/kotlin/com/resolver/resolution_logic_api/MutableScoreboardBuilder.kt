package com.resolver.resolution_logic_api

interface MutableScoreboardBuilder<T : MutableRow> {
    fun build(eventFeedAnalyzer: EventFeedAnalyzer): MutableScoreboard<T>
}
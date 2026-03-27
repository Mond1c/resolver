package com.resolver

interface MutableScoreboardBuilder<T : MutableRow> {
    fun build(eventFeedAnalyzer: EventFeedAnalyzer): MutableScoreboard<T>
}
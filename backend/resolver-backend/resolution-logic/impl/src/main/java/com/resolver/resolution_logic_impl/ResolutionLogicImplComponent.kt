package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.EventFeedResolutionPreparator
import com.resolver.resolution_logic_api.MutableICPCRow
import com.resolver.resolution_logic_api.MutableScoreboardBuilder
import kotlinx.serialization.json.Json
import org.icpclive.clics.FeedVersion
import org.icpclive.clics.clicsEventsSerializersModule
import java.nio.file.Path

object ResolutionLogicImplComponent {
    val json by lazy {
        Json {
            serializersModule = clicsEventsSerializersModule(
                feedVersion = FeedVersion.`2023_06`,
                tokenPrefix = ""
            )
        }
    }

    internal val eventFeedAnalyzer1 by lazy {
        EventFeedAnalyzerImpl(
            json = json
        )
    }

    internal fun provideMutableICPCScoreboardBuilder1(): MutableScoreboardBuilder<MutableICPCRow> {
        return MutableICPCScoreboardBuilderImpl
    }

    fun provideICPCEventFeedResolutionPreparator1(eventFeedPath: Path): EventFeedResolutionPreparator {
        return SimpleICPCEventFeedResolutionPreparatorImpl(
            eventFeedAnalyzer = eventFeedAnalyzer1,
            mutableICPCScoreboardBuilder = provideMutableICPCScoreboardBuilder1(),
            eventFeedPath = eventFeedPath
        )
    }
}
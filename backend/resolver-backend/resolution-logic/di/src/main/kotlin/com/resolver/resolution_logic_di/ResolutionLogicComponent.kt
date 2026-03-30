package com.resolver.resolution_logic_di

import com.resolver.resolution_logic_api.EventFeedResolutionPreparator
import com.resolver.resolution_logic_impl.di.ResolutionLogicImplComponent
import java.nio.file.Path

object ResolutionLogicComponent {
    val json by lazy {
        ResolutionLogicImplComponent.json
    }

    fun provideEventFeedResolutionPreparator1(
        eventFeedPath: Path
    ): EventFeedResolutionPreparator {
        return ResolutionLogicImplComponent
            .provideICPCEventFeedResolutionPreparator1(eventFeedPath)
    }
}
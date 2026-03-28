package com.resolver

import com.resolver.resolution_logic_di.ResolutionLogicComponent
import kotlin.io.path.Path

fun main() {
    val preparator = ResolutionLogicComponent
        .provideEventFeedResolutionPreparator1(Path("./data/event-feed.ndjson"))
    preparator.prepareResolution().subList(0, 4).also { println(it.joinToString(separator = "\n\n")) }
}
package com.resolver

import com.resolver.resolution_logic_di.ResolutionLogicComponent
import kotlin.io.path.Path

fun main() {
    val preparator = ResolutionLogicComponent
        .provideEventFeedResolutionPreparator1(Path("./data/fail-pass-test-event-feed-1.ndjson"))
    preparator.prepareResolution().also { println(it.joinToString(separator = "\n\n")) }
}
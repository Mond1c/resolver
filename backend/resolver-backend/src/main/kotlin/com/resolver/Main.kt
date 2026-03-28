package com.resolver

import com.resolver.resolution_logic_di.ResolutionLogicComponent
import org.icpclive.clics.objects.Judgement
import kotlin.io.path.Path

fun main() {
    ResolutionLogicComponent.json.decodeFromString<Judgement>("""
        {"id":"12398","submission_id":"6866","judgement_type_id":"WA","start_contest_time":"0:06:40.872","start_time":"2024-09-19T11:57:59.872+06:00","end_contest_time":"0:06:44.326","end_time":"2024-09-19T11:58:03.326+06:00"}
    """.trimIndent()).also {
        println(it)
    }
    val preparator = ResolutionLogicComponent
        .provideEventFeedResolutionPreparator1(Path("./data/event-feed.ndjson"))
    preparator.prepareResolution().subList(0, 10).also { println(it.joinToString(separator = "\n\n")) }
}
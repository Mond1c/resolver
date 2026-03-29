package com.resolver.resolution_logic_impl.event_feed_analyzer

import com.resolver.resolution_logic_impl.Constants.DOT_PRODUCT
import com.resolver.resolution_logic_impl.Constants.MAGIC_KUBES
import com.resolver.resolution_logic_impl.Constants.MAGIC_SQUARES
import com.resolver.resolution_logic_impl.Constants.TERNARY_EXPONENTIATION
import com.resolver.resolution_logic_impl.Constants.TERNARY_HEAP
import com.resolver.resolution_logic_impl.Constants.TERNARY_SEARCH_TREE
import com.resolver.resolution_logic_impl.di.ResolutionLogicImplComponent
import org.junit.jupiter.api.assertAll
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EventFeedAnalyzerImplTests {
    @Test
    fun testFirstToSolve1() {
        val ndjsonUrl =
            javaClass.getResource("/fail-pass-test-event-feed-1.ndjson")
                ?: fail("File not found in resources")
        val path = Paths.get(ndjsonUrl.toURI())
        val eventFeedAnalyzer = ResolutionLogicImplComponent.eventFeedAnalyzer1
        eventFeedAnalyzer.resetAndAnalyze(path)
        val problemIdToFirstSolvedTeamId = eventFeedAnalyzer.getProblemIdToFirstSolvedTeamId()
        assertAll(
            {
                assertEquals("1", problemIdToFirstSolvedTeamId[TERNARY_HEAP])
            },
            {
                assertEquals("1", problemIdToFirstSolvedTeamId[TERNARY_SEARCH_TREE])
            },
            {
                assertEquals("3", problemIdToFirstSolvedTeamId[TERNARY_EXPONENTIATION])
            },
            {
                assertEquals("3", problemIdToFirstSolvedTeamId[MAGIC_SQUARES])
            },
            {
                assertEquals("3", problemIdToFirstSolvedTeamId[MAGIC_KUBES])
            },
            {
                assertEquals("5", problemIdToFirstSolvedTeamId[DOT_PRODUCT])
            }
        )
    }
}
package org.icpclive.resolver.scoreboard_management_impl

import com.github.ajalt.clikt.core.main
import org.icpclive.resolver.resolution_logic_di.ResolutionLogicComponent
import org.icpclive.resolver.scoreboard_management_di.ScoreboardManagementComponent
import org.icpclive.resolver.util_di.ResolverUtilComponent
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertTrue

class UiMapperImplTest {
    @Test
    fun `GIVEN resolution steps WHEN map it to ui events THEN result sequence is valid`() {
        ResolverUtilComponent.provideAppBase(
            ResolverUtilComponent.scoreboardCalculator,
            ResolverUtilComponent.yesNoConsoleHandler,
            ScoreboardManagementComponent.json
        ) { _, map, _, frozen, notFrozen ->
            val frozenState = frozen.last()
            val resolver = ResolutionLogicComponent.provideResolver(frozenState)
            val result = resolver.resolve(frozenState, notFrozen, map)
            val uiEvents = ScoreboardManagementComponent.uiMapper mapToUiEvents result.steps
            assertTrue {
                UiEventSequenceValidator.isUiEventSequenceValid(uiEvents)
            }
        }.main(listOf("-c", Paths.get(this::class.java.getResource("/1")!!.toURI()).toString()))
    }
}
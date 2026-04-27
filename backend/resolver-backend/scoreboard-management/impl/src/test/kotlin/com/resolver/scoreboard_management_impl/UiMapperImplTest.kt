package com.resolver.scoreboard_management_impl

import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.testing.test
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.util_api.App
import com.resolver.util_di.ResolverUtilComponent
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.runTest
import org.icpclive.cds.api.ContestState
import kotlin.test.Test

class UiMapperImplTest {
    private class AppTest : App() {
        private val options by ResolverUtilComponent.resolverCommandLineOptions

        override fun run() = runTest {
            val contestStatesLoader = ResolverUtilComponent.provideContestStatesLoader(
                cdsCommandLineOptions = options
            )
            val semaphore = Semaphore(2, 2)
            val frozen = mutableListOf<ContestState>()
            val frozenJob = contestStatesLoader.loadContestStates(
                scope = this,
                dst = frozen,
                semaphore = semaphore,
                submissionResultsAfterFreezeInput = false
            )
            val notFrozen = mutableListOf<ContestState>()
            val notFrozenJob = contestStatesLoader.loadContestStates(
                scope = this,
                dst = notFrozen,
                semaphore = semaphore,
                submissionResultsAfterFreezeInput = true
            )
            semaphore.acquire()
            semaphore.acquire()
            frozenJob.cancel()
            notFrozenJob.cancel()
            val frozenState = frozen.last()
            val resolver = ResolutionLogicComponent.provideResolver(frozenState)
            val result = resolver.resolve(frozenState, notFrozen, mapOf())
            println("AAA " + options.configDirectory + " AAA")
        }
    }

    @Test
    fun `GIVEN resolution steps WHEN map it to ui events THEN result sequence is valid`() {
        AppTest().main("-c /1")
    }
}
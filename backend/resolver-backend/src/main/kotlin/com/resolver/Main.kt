package com.resolver

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.resolver_server_api.StartResult
import com.resolver.resolver_server_di.ResolverServerComponent
import com.resolver.scoreboard_management_di.ScoreboardManagementComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.icpclive.cds.CommentaryMessagesUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ContestStatus

object App : CliktCommand() {
    private val resolverOptions by ResolverCommandLineOptions()

    override fun run() {
        val frozen = mutableListOf<ContestState>()
        val notFrozen = mutableListOf<ContestState>()
        val semaphore = Semaphore(2, 2)
        runBlocking {
            val frozenJob = launch {
                loadContestStates(
                    dst = frozen,
                    semaphore = semaphore,
                    submissionResultsAfterFreezeInput = false
                )
            }

            val notFrozenJob = launch {
                loadContestStates(
                    dst = notFrozen,
                    semaphore = semaphore,
                    submissionResultsAfterFreezeInput = true
                )
            }

            semaphore.acquire()
            semaphore.acquire()

            frozenJob.cancel()
            notFrozenJob.cancel()

            val frozenState = frozen.last()
            val resolver = ResolutionLogicComponent.greedyICPCResolver
            val result = resolver.resolve(notFrozen)
            val manager = ScoreboardManagementComponent.provideScoreboardManager1(
                frozenState = frozenState,
                snapshots = result.snapshots,
                steps = result.steps
            )
            val server = ResolverServerComponent.provideServer1(
                manager,
                ScoreboardManagementComponent.json
            )
            when (val startResult = server.start(resolverOptions.port, resolverOptions.host)) {
                StartResult.AlreadyStarted -> {}
                StartResult.Failure -> {}
                is StartResult.MaybeSuccess -> {
                    startResult.startJob.join()
                }
            }
        }
    }

    private suspend fun loadContestStates(
        dst: MutableList<ContestState>,
        semaphore: Semaphore,
        submissionResultsAfterFreezeInput: Boolean
    ) {
        resolverOptions.toFlow().addComputedData {
            firstToSolves = true
            submissionResultsAfterFreeze = submissionResultsAfterFreezeInput
            autoFinalize = true
        }
            .contestState()
            .collect { state ->
                when (val lastEvent = state.lastEvent) {
                    is CommentaryMessagesUpdate -> {}
                    is InfoUpdate -> {
                        if (lastEvent.newInfo.status is ContestStatus.OVER) {
                            dst.add(state)
                            semaphore.release()
                        }
                    }

                    is RunUpdate -> {
                        dst.add(state)
                    }
                }
            }
    }
}

fun main(args: Array<String>) = App.main(args)
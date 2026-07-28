package org.icpclive.resolver.util_impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import org.icpclive.cds.CommentaryMessagesUpdate
import org.icpclive.cds.InfoUpdate
import org.icpclive.cds.RunUpdate
import org.icpclive.cds.adapters.addComputedData
import org.icpclive.cds.adapters.contestState
import org.icpclive.cds.api.ContestState
import org.icpclive.cds.api.ContestStatus
import org.icpclive.cds.cli.CdsCommandLineOptions
import org.icpclive.resolver.util_api.ContestStatesLoader

class ContestStatesLoaderImpl(
    private val cdsCommandLineOptions: CdsCommandLineOptions
) : ContestStatesLoader {
    override fun loadContestStates(
        scope: CoroutineScope,
        dst: MutableList<ContestState>,
        semaphore: Semaphore,
        submissionResultsAfterFreezeInput: Boolean
    ): Job {
        return scope.launch {
            cdsCommandLineOptions.toFlow().addComputedData {
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
}
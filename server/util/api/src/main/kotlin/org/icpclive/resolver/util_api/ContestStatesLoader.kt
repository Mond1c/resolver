package org.icpclive.resolver.util_api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Semaphore
import org.icpclive.cds.api.ContestState

interface ContestStatesLoader {
    fun loadContestStates(
        scope: CoroutineScope,
        dst: MutableList<ContestState>,
        semaphore: Semaphore,
        submissionResultsAfterFreezeInput: Boolean
    ): Job
}
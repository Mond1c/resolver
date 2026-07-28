package org.icpclive.resolver.resolver_server_api

import kotlinx.coroutines.Job

sealed interface StartResult {
    class MaybeSuccess(
        val startJob: Job
    ) : StartResult

    object Failure : StartResult

    object AlreadyStarted : StartResult
}
package com.resolver.resolver_server_api

sealed interface StopResult {
    object Success : StopResult

    object Failure : StopResult

    object AlreadyStopped : StopResult
}
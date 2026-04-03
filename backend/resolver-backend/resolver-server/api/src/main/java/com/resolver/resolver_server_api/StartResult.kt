package com.resolver.resolver_server_api

sealed interface StartResult {
    object Success : StartResult

    object Failure : StartResult

    object AlreadyStarted : StartResult
}
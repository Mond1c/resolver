package com.resolver.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow

@ContributesTo(AppScope::class)
@BindingContainer
internal object ControllerToServerMessageContainer {
    @SingleIn(AppScope::class)
    @Provides
    fun provideControllerToServerMessageFlow(): MutableSharedFlow<String> {
        return MutableSharedFlow(replay = 1)
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideConnectionFailureFlow(): MutableSharedFlow<Unit> {
        return MutableSharedFlow()
    }
}
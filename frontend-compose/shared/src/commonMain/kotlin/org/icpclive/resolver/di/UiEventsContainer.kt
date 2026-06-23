package org.icpclive.resolver.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import org.icpclive.resolver.UiEvent
import org.icpclive.resolver.config.ConfigLoader
import org.icpclive.resolver.getEngine

@ContributesTo(AppScope::class)
@BindingContainer
internal object UiEventsContainer {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(getEngine()) {
            install(WebSockets)
        }
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
        }
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideIsConnectedFlow(): MutableStateFlow<Boolean> {
        return MutableStateFlow(false)
    }

    @SingleIn(AppScope::class)
    @Provides
    fun provideUiEventsFlow(
        json: Json,
        client: HttpClient,
        configLoader: ConfigLoader,
        isConnectedFlow: MutableStateFlow<Boolean>
    ): Flow<UiEvent> {
        return flow {
            val config = configLoader.loadConfig()
            while (currentCoroutineContext().isActive) {
                runCatching {
                    client.webSocket(config.resolutionUrl) {
                        isConnectedFlow.emit(true)
                        for (jsonEvent in incoming) {
                            emit(
                                json.decodeFromString<UiEvent>(jsonEvent.data.decodeToString())
                            )
                        }
                    }
                }
                isConnectedFlow.emit(false)
                delay(config.resolutionReconnectTimeMs)
            }
        }
    }
}
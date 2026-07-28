package org.icpclive.resolver.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.icpclive.resolver.ServerToControllerMessage
import org.icpclive.resolver.config.ConfigLoader

@ContributesTo(AppScope::class)
@BindingContainer
internal object ServerToControllerMessageContainer {
    @SingleIn(AppScope::class)
    @Provides
    fun provideServerToControllerMessageFlow(
        json: Json,
        client: HttpClient,
        controllerToServerMessageFlow: MutableSharedFlow<String>,
        connectionFailureFlow: MutableSharedFlow<Unit>,
        configLoader: ConfigLoader
    ): Flow<ServerToControllerMessage> {
        return callbackFlow {
            val config = configLoader.loadConfig()
            while (isActive) {
                runCatching {
                    val auth = controllerToServerMessageFlow.first()
                    client.webSocket(config.resolutionControllerUrl) {
                        send(Frame.Text(auth))
                        val sendJob = launch {
                            controllerToServerMessageFlow.collect { msg ->
                                send(Frame.Text(msg))
                            }
                        }
                        try {
                            for (jsonEvent in incoming) {
                                try {
                                    send(
                                        json.decodeFromString<ServerToControllerMessage>(
                                            jsonEvent.data.decodeToString()
                                        )
                                    )
                                } catch (_: SerializationException) {
                                }
                            }
                        } finally {
                            sendJob.cancel()
                        }
                    }
                }
                connectionFailureFlow.emit(Unit)
                delay(config.resolutionControllerReconnectTimeMs)
            }
        }
    }
}
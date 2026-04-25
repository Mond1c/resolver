package com.resolver.resolver_server_impl

import com.resolver.scoreboard_management_api.ScoreboardManager
import com.resolver.scoreboard_management_api.UiEvent
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json

internal class ResolutionRoom(
    private val scoreboardManager: ScoreboardManager,
    private val json: Json,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val eventsFlow = scoreboardManager.getUiEventsFlow()
        .map { json.encodeToString(it) }
        .shareIn(scope, SharingStarted.Eagerly)

    fun addClient(session: DefaultWebSocketSession): Job {
        return scope.launch {
            try {
                session.send(json.encodeToString<UiEvent>(scoreboardManager.getScoreboard()))
                eventsFlow.collect { serializedEvent ->
                    session.send(serializedEvent)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }
}
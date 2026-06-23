package org.icpclive.resolver.resolver_server_impl

import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.icpclive.resolver.UiEvent
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.scoreboard_management_api.toCore

internal class ResolutionRoom(
    private val scoreboardManager: ScoreboardManager,
    private val json: Json,
    replay: Int,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val eventsFlow = scoreboardManager.getUiEventsFlow()
        .map { json.encodeToString(it.toCore()) }
        .shareIn(scope, SharingStarted.Eagerly, replay)

    fun addClient(session: DefaultWebSocketSession): Job {
        return session.launch {
            try {
                session.send(
                    json.encodeToString<UiEvent>(
                        scoreboardManager.getScoreboard().toCore()
                    )
                )
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
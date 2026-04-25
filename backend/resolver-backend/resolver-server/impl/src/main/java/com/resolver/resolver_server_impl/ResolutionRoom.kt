package com.resolver.resolver_server_impl

import com.resolver.scoreboard_management_api.ScoreboardManager
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

internal class ResolutionRoom(
    private val scoreboardManager: ScoreboardManager,
    private val json: Json,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val clients = ConcurrentHashMap.newKeySet<DefaultWebSocketSession>()

    init {
        scope.launch {
            scoreboardManager.getUiEventsFlow()
                .collect { event ->
                    for (client in clients) {
                        try {
                            client.send(json.encodeToString(event))
                        } catch (_: Exception) {
                            clients.remove(client)
                        }
                    }
                }
        }
    }

    fun addClient(session: DefaultWebSocketSession) {
        clients.add(session)
    }
}
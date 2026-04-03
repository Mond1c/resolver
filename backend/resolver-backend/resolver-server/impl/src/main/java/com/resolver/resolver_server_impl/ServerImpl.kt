package com.resolver.resolver_server_impl

import com.resolver.resolver_server_api.Server
import com.resolver.resolver_server_api.StartResult
import com.resolver.resolver_server_api.StopResult
import com.resolver.scoreboard_management_api.ScoreboardManager
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class ServerImpl(
    private val json: Json,
    scoreboardManager: ScoreboardManager
) : Server(scoreboardManager) {
    private lateinit var server: EmbeddedServer<*, *>
    private var isStarted = AtomicBoolean(false)
    private val mtx = Mutex()

    override suspend fun start(port: Int, host: String): StartResult {
        try {
            mtx.withLock {
                if (!::server.isInitialized) {
                    server = embeddedServer(
                        Netty,
                        port = port,
                        host = host
                    ) { module() }
                        .start(wait = true)
                    isStarted.store(true)
                    return StartResult.Success
                }
                if (isStarted.load()) {
                    return StartResult.AlreadyStarted
                }
                server.startSuspend(wait = true)
                isStarted.store(true)
                return StartResult.Success
            }
        } catch (_: Exception) {
            return StartResult.Failure
        }
    }

    override suspend fun stop(): StopResult {
        try {
            mtx.withLock {
                if (isStarted.load() && ::server.isInitialized) {
                    server.stopSuspend()
                    isStarted.store(false)
                    return StopResult.Success
                }
                return StopResult.AlreadyStopped
            }
        } catch (_: Exception) {
            return StopResult.Failure
        }
    }

    private fun Application.module() {
        install(WebSockets) {
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
        }

        routing {
            setResolutionWebSocketRoute()
            setResolutionControlWebSocketRoute()
        }
    }

    private fun Routing.setResolutionWebSocketRoute() {
        webSocket(RESOLUTION_WS_ENDPOINT) {
//                send(json.encodeToString(scoreboardManager.getScoreboard()))
            scoreboardManager.getUiEventsFlow()
                .collect { event ->
                    send(json.encodeToString(event))
                }
        }
    }

    private fun Routing.setResolutionControlWebSocketRoute() {
        webSocket(RESOLUTION_CONTROL_WS_ENDPOINT) {
            runCatching {
                incoming.consumeEach { frame ->
                    // 0 - stop, 1 - start, 2 - up, 3 - down, 4 x - apply speed factor x
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        if (receivedText.contains(' ')) {
                            val parts = receivedText.split(' ')
                            if (parts.size == 2) {
                                if (parts[0] == "4") {
                                    parts[1].toDoubleOrNull()?.let { factor ->
                                        scoreboardManager.applySpeedFactor(factor)
                                    }
                                }
                            }
                        }
                        when (receivedText) {
                            "0" -> scoreboardManager.stop()
                            "1" -> scoreboardManager.start()
                            "2" -> scoreboardManager.up()
                            "3" -> scoreboardManager.down()
                        }
                    }
                }
            }
        }
    }
}
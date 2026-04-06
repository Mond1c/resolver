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
import kotlinx.coroutines.*
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
    scoreboardManager: ScoreboardManager,
    serverDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Server(scoreboardManager) {
    private lateinit var server: EmbeddedServer<*, *>
    private val isStarted = AtomicBoolean(false)
    private val mtx = Mutex()
    private val serverScope = CoroutineScope(SupervisorJob() + serverDispatcher)

    override suspend fun start(port: Int, host: String): StartResult {
        return try {
            mtx.withLock {
                if (isStarted.load()) {
                    return StartResult.AlreadyStarted
                }
                server = embeddedServer(
                    Netty,
                    port = port,
                    host = host
                ) { module() }
                val startJob = serverScope.launch {
                    try {
                        server.startSuspend(wait = true)
                    } catch (e: Exception) {
                        isStarted.store(false)
                        throw e
                    }
                }
                isStarted.store(true)
                StartResult.MaybeSuccess(startJob)
            }
        } catch (_: Exception) {
            StartResult.Failure
        }
    }

    override suspend fun stop(): StopResult {
        return try {
            mtx.withLock {
                if (isStarted.load()) {
                    server.stopSuspend()
                    isStarted.store(false)
                    return StopResult.Success
                }
                StopResult.AlreadyStopped
            }
        } catch (_: Exception) {
            StopResult.Failure
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
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        val parts = receivedText.split(SPACE)
                        if (parts.size == 2) {
                            if (parts[0] == SIG_APPLY_FACTOR) {
                                parts[1].toDoubleOrNull()?.let { factor ->
                                    scoreboardManager.applySpeedFactor(factor)
                                }
                            }
                        }
                        with(scoreboardManager) {
                            when (receivedText) {
                                SIG_STOP -> stop()
                                SIG_START -> start()
                                SIG_UP -> up()
                                SIG_DOWN -> down()
                                SIG_CHANGE_DIRECTION -> changeDirection()
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val SIG_STOP = "0"
        private const val SIG_START = "1"
        private const val SIG_UP = "2"
        private const val SIG_DOWN = "3"
        private const val SIG_APPLY_FACTOR = "4"
        private const val SIG_CHANGE_DIRECTION = "5"
        private const val SPACE = ' '
    }
}
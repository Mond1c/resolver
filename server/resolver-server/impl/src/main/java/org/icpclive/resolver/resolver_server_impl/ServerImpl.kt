package org.icpclive.resolver.resolver_server_impl

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.icpclive.resolver.resolver_server_api.Server
import org.icpclive.resolver.resolver_server_api.ServerOptions
import org.icpclive.resolver.resolver_server_api.StartResult
import org.icpclive.resolver.resolver_server_api.StartServerOptions
import org.icpclive.resolver.resolver_server_api.StopResult
import org.icpclive.resolver.scoreboard_management_api.ScoreboardManager
import org.icpclive.resolver.util_api.ResolverAccounts
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class ServerImpl(
    private val json: Json,
    serverOptions: ServerOptions,
    scoreboardManager: ScoreboardManager,
    resolverAccounts: ResolverAccounts,
    serverDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Server(scoreboardManager, serverOptions, resolverAccounts) {
    private lateinit var server: EmbeddedServer<*, *>
    private val isStarted = AtomicBoolean(false)
    private val mtx = Mutex()
    private val serverScope = CoroutineScope(SupervisorJob() + serverDispatcher)
    private val controlRoom = ResolutionControlRoom(
        json = json,
        scoreboardManagerSettingsFlow = scoreboardManager.getSettingsFlow(),
        isAuthDisabled = serverOptions.isAuthDisabled
    )
    private val resolutionRoom = ResolutionRoom(
        scoreboardManager = scoreboardManager,
        json = json,
        replay = serverOptions.replay
    )

    override suspend fun start(startServerOptions: StartServerOptions): StartResult {
        return try {
            mtx.withLock {
                if (isStarted.load()) {
                    return StartResult.AlreadyStarted
                }
                server = embeddedServer(
                    Netty,
                    port = startServerOptions.port,
                    host = startServerOptions.host
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
        webSocket(serverOptions.resolutionWsEndpoint) {
            resolutionRoom.addClient(this).join()
        }
    }

    private fun Routing.setResolutionControlWebSocketRoute() {
        webSocket(serverOptions.resolutionControlWsEndpoint) {
            with(scoreboardManager) {
                controlRoom.setBehaviour(
                    session = this@webSocket,
                    onValidateCredentials = { login, password ->
                        resolverAccounts.contains(
                            login,
                            password
                        )
                    },
                    onStart = ::start,
                    onStop = ::stop,
                    onUp = ::up,
                    onDown = ::down,
                    onChangeDirection = ::changeDirection,
                    onApplyFactor = ::applySpeedFactor,
                    onGetVariantsToGoto = ::getVariantsToGoto,
                    onGoto = { stateIndex, teamId -> goto(stateIndex, teamId) }
                )
            }
        }
    }
}
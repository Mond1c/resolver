package com.resolver.resolver_server_impl

import com.github.ajalt.clikt.core.main
import com.resolver.resolution_logic_di.ResolutionLogicComponent
import com.resolver.resolver_server_api.ServerOptions
import com.resolver.resolver_server_api.StartServerOptions
import com.resolver.resolver_server_di.ResolverServerComponent
import com.resolver.scoreboard_management_api.ScoreboardManagerOptions
import com.resolver.scoreboard_management_api.UiEvent
import com.resolver.scoreboard_management_di.ScoreboardManagementComponent
import com.resolver.util_di.ResolverUtilComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertTrue

class ServerImplTest {
    @Test
    fun `GIVEN resolution in process WHEN client joins THEN no one of important ui events is lost`() = runBlocking {
        ResolverUtilComponent.provideAppBase(
            ResolverUtilComponent.scoreboardCalculator1,
            ResolverUtilComponent.yesNoConsoleHandler,
            ScoreboardManagementComponent.json
        ) { passwords, map, options, frozen, notFrozen ->
            val frozenState = frozen.last()
            val resolver = ResolutionLogicComponent.provideResolver(frozenState)
            val result = resolver.resolve(frozenState, notFrozen, map)
            val manager = ScoreboardManagementComponent.provideScoreboardManager1(
                frozenState,
                result.snapshots,
                result.steps,
                ScoreboardManagerOptions(
                    baseTimeBetweenMs = options.baseTimeBetweenMs,
                    isGotoEnabled = options.isGotoEnabled,
                    replay = options.replay
                )
            )
            val json = Json {
                ignoreUnknownKeys = true
            }
            val server = ResolverServerComponent.provideServer1(
                manager,
                json,
                ServerOptions(
                    resolutionControlWsEndpoint = options.resolutionControlWsEndpoint,
                    resolutionWsEndpoint = options.resolutionWsEndpoint,
                    replay = options.replay
                ),
                passwords
            )
            server.start(
                StartServerOptions(
                    host = options.host,
                    port = options.port
                )
            )
            delay(7000)
            var isReady1 = false
            var scoreboard1: UiEvent.Scoreboard? = null
            val uiEvents1 = mutableListOf<UiEvent>()
            val semaphore = Semaphore(2, 2)
            launch(SupervisorJob()) {
                HttpClient(CIO) { install(WebSockets) }.use {
                    it.webSocket(
                        options.resolutionWsEndpoint.createWsUrlString(options.host, options.port)
                    ) {
                        isReady1 = true
                        scoreboard1 = json.decodeFromString(incoming.receive().data.decodeToString())
                        launch {
                            incoming.consumeEach { event ->
                                uiEvents1.add(json.decodeFromString(event.data.decodeToString()))
                            }
                        }
                        semaphore.acquire()
                        close()
                    }
                }
            }
            while (!isReady1) {
                delay(300)
            }
            manager.start()
            manager.applySpeedFactor(10.0)
            delay(3000)
            var isReady2 = false
            var scoreboard2: UiEvent.Scoreboard? = null
            val uiEvents2 = mutableListOf<UiEvent>()
            launch(SupervisorJob()) {
                HttpClient(CIO) { install(WebSockets) }.use {
                    it.webSocket(
                        options.resolutionWsEndpoint.createWsUrlString(options.host, options.port)
                    ) {
                        isReady2 = true
                        scoreboard2 = json.decodeFromString(incoming.receive().data.decodeToString())
                        launch {
                            incoming.consumeEach { event ->
                                uiEvents2.add(json.decodeFromString(event.data.decodeToString()))
                            }
                        }
                        semaphore.acquire()
                        close()
                    }
                }
            }
            while (!isReady2) {
                delay(300)
            }
            delay(5000)
            manager.stop()
            delay(3000)
            semaphore.release()
            semaphore.release()
            delay(1000)
            server.stop()
            val rows1 = scoreboard1!!.teamIdToScoreboardRow.applyUiEvents(uiEvents1)
            val rows2 = scoreboard2!!.teamIdToScoreboardRow.applyUiEvents(uiEvents2)
            assertTrue {
                rows1 == rows2
            }
        }.main(listOf("-c", Paths.get(this::class.java.getResource("/1")!!.toURI()).toString()))
    }
}
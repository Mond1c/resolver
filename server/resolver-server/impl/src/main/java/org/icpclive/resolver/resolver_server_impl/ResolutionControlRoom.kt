package org.icpclive.resolver.resolver_server_impl

import io.ktor.websocket.CloseReason
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.TeamId
import org.icpclive.cds.api.toTeamId
import org.icpclive.resolver.ServerToControllerMessage
import org.icpclive.resolver.Signals.SIG_APPLY_FACTOR
import org.icpclive.resolver.Signals.SIG_CHANGE_DIRECTION
import org.icpclive.resolver.Signals.SIG_DOWN
import org.icpclive.resolver.Signals.SIG_GET_VARIANTS_TO_GOTO
import org.icpclive.resolver.Signals.SIG_GOTO
import org.icpclive.resolver.Signals.SIG_START
import org.icpclive.resolver.Signals.SIG_STOP
import org.icpclive.resolver.Signals.SIG_UP
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

internal class ResolutionControlRoom(
    private val json: Json,
    private val scoreboardManagerSettingsFlow: StateFlow<ServerToControllerMessage.ScoreboardManagerSettings>,
) {
    private val clients = ConcurrentHashMap.newKeySet<String>()

    private suspend fun DefaultWebSocketSession.closeWithAuthFailure() =
        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, AUTH_FAILED_MSG))

    suspend fun setBehaviour(
        session: DefaultWebSocketSession,
        onValidateCredentials: (login: String?, password: String?) -> Boolean,
        onStart: () -> Unit,
        onStop: () -> Unit,
        onUp: () -> Unit,
        onDown: () -> Unit,
        onChangeDirection: () -> Unit,
        onApplyFactor: (Double) -> Unit,
        onGetVariantsToGoto: (TeamId) -> ServerToControllerMessage.VariantsToGoto?,
        onGoto: (Int, TeamId) -> Unit
    ) {
        with(session) {
            runCatching {
                val login = validateCredentials(onValidateCredentials) ?: return@runCatching
                try {
                    launch {
                        scoreboardManagerSettingsFlow.collect {
                            send(json.encodeToString<ServerToControllerMessage>(it))
                        }
                    }
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val receivedText = frame.readText()
                            val parts = receivedText.split(SPACE)
                            if (parts.isNotEmpty()) {
                                when (parts[0]) {
                                    SIG_STOP -> onStop()
                                    SIG_START -> onStart()
                                    SIG_UP -> onUp()
                                    SIG_DOWN -> onDown()
                                    SIG_CHANGE_DIRECTION -> onChangeDirection()

                                    SIG_APPLY_FACTOR -> {
                                        if (parts.size == 2) {
                                            parts[1].toDoubleOrNull()
                                                ?.let(onApplyFactor)
                                        }
                                    }

                                    SIG_GET_VARIANTS_TO_GOTO -> {
                                        if (parts.size == 2) {
                                            send(
                                                json.encodeToString<ServerToControllerMessage?>(
                                                    onGetVariantsToGoto(parts[1].toTeamId())
                                                )
                                            )
                                        }
                                    }

                                    SIG_GOTO -> {
                                        if (parts.size == 3) {
                                            parts[1].toIntOrNull()?.let { stateIndex ->
                                                onGoto(stateIndex, parts[2].toTeamId())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    clients.remove(login)
                }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.validateCredentials(
        onValidateCredentials: (login: String?, password: String?) -> Boolean
    ): String? {
        val creds = withTimeout(AUTH_TIMEOUT) {
            incoming.receive() as? Frame.Text
        }
        val parts = creds?.readText()?.split(SPACE)
        if (parts == null || parts.size != 2) {
            closeWithAuthFailure()
            return null
        }
        val (login, password) = parts
        if (!onValidateCredentials(login, password) || !clients.add(login)) {
            closeWithAuthFailure()
            return null
        }
        return login
    }

    companion object {
        private const val AUTH_FAILED_MSG = "Authentication failed"
        private val AUTH_TIMEOUT = 60.seconds
        private const val SPACE = ' '
    }
}
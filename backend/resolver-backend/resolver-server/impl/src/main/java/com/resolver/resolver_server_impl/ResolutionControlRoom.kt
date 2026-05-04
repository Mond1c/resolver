package com.resolver.resolver_server_impl

import com.resolver.scoreboard_management_api.ServerToControllerMessage
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.icpclive.cds.api.TeamId
import org.icpclive.cds.api.toTeamId
import java.util.concurrent.ConcurrentHashMap

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
                            send(json.encodeToString(it))
                        }
                    }
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val receivedText = frame.readText()
                            val parts = receivedText.split(SPACE)
                            if (parts.size == 2) {
                                when (parts[0]) {
                                    SIG_APPLY_FACTOR -> parts[1].toDoubleOrNull()?.let(onApplyFactor)
                                    SIG_GET_VARIANTS_TO_GOTO -> {
                                        send(
                                            json.encodeToString<ServerToControllerMessage?>(
                                                onGetVariantsToGoto(parts[1].toTeamId())
                                            )
                                        )
                                    }
                                }
                            } else if (parts.size == 3) {
                                when (parts[0]) {
                                    SIG_GOTO -> parts[1].toIntOrNull()?.let { stateIndex ->
                                        onGoto(stateIndex, parts[2].toTeamId())
                                    }
                                }
                            }
                            when (receivedText) {
                                SIG_STOP -> onStop()
                                SIG_START -> onStart()
                                SIG_UP -> onUp()
                                SIG_DOWN -> onDown()
                                SIG_CHANGE_DIRECTION -> onChangeDirection()
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
        onValidatePassword: (login: String?, password: String?) -> Boolean
    ): String? {
        val creds = withTimeout(AUTH_TIMEOUT_MS) {
            incoming.receive() as? Frame.Text
        }
        val parts = creds?.readText()?.split(SPACE)
        if (parts == null || parts.size != 2) {
            closeWithAuthFailure()
            return null
        }
        val (login, password) = parts
        if (!onValidatePassword(login, password) || !clients.add(login)) {
            closeWithAuthFailure()
            return null
        }
        return login
    }

    companion object {
        private const val AUTH_FAILED_MSG = "Authentication failed"
        private const val AUTH_TIMEOUT_MS = 60_000L
        private const val SIG_STOP = "0"
        private const val SIG_START = "1"
        private const val SIG_UP = "2"
        private const val SIG_DOWN = "3"
        private const val SIG_APPLY_FACTOR = "4"
        private const val SIG_CHANGE_DIRECTION = "5"
        private const val SIG_GET_VARIANTS_TO_GOTO = "6"
        private const val SIG_GOTO = "7"
        private const val SPACE = ' '
    }
}
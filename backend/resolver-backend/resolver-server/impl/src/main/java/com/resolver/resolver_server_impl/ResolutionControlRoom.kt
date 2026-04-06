package com.resolver.resolver_server_impl

import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach

internal class ResolutionControlRoom {
    suspend fun setBehaviour(
        session: DefaultWebSocketSession,
        onStart: () -> Unit,
        onStop: () -> Unit,
        onUp: () -> Unit,
        onDown: () -> Unit,
        onChangeDirection: () -> Unit,
        onApplyFactor: (Double) -> Unit
    ) {
        with(session) {
            runCatching {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val receivedText = frame.readText()
                        val parts = receivedText.split(SPACE)
                        if (parts.size == 2) {
                            if (parts[0] == SIG_APPLY_FACTOR) {
                                parts[1].toDoubleOrNull()?.let { factor ->
                                    onApplyFactor(factor)
                                }
                            }
                        }
                        when (receivedText) {
                            SIG_STOP -> onStop()
                            SIG_START -> onStart()
                            SIG_UP -> onUp()
                            SIG_DOWN -> onDown()
                            SIG_CHANGE_DIRECTION -> onChangeDirection()
                            else -> {}
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
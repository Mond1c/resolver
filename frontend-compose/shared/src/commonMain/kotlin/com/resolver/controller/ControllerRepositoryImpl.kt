package com.resolver.controller

import com.resolver.ControllerRepository
import com.resolver.ServerToControllerMessage
import com.resolver.Signals
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class ControllerRepositoryImpl(
    private val serverToControllerMessageFlow: Flow<ServerToControllerMessage>,
    private val controllerToServerMessageFlow: MutableSharedFlow<String>,
    private val connectionFailureFlow: MutableSharedFlow<Unit>,
    scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
                CoroutineExceptionHandler { _, _ -> })
) : ControllerRepository {
    private val variantsToGotoFlow = MutableSharedFlow<ServerToControllerMessage.VariantsToGoto?>()
    private val settingsFlow: MutableSharedFlow<ServerToControllerMessage.ScoreboardManagerSettings?> =
        MutableSharedFlow()

    init {
        scope.launch {
            serverToControllerMessageFlow
                .collect { msg ->
                    when (msg) {
                        is ServerToControllerMessage.ScoreboardManagerSettings -> settingsFlow.emit(
                            msg
                        )

                        is ServerToControllerMessage.VariantsToGoto -> variantsToGotoFlow.emit(
                            msg
                        )
                    }
                }
        }
        scope.launch {
            connectionFailureFlow.collect {
                variantsToGotoFlow.emit(null)
                settingsFlow.emit(null)
            }
        }
    }

    override suspend fun stop() {
        controllerToServerMessageFlow.emit(Signals.SIG_STOP)
    }

    override suspend fun start() {
        controllerToServerMessageFlow.emit(Signals.SIG_START)
    }

    override suspend fun up() {
        controllerToServerMessageFlow.emit(Signals.SIG_UP)
    }

    override suspend fun down() {
        controllerToServerMessageFlow.emit(Signals.SIG_DOWN)
    }

    override suspend fun applyFactor(factor: Double) {
        controllerToServerMessageFlow.emit("${Signals.SIG_APPLY_FACTOR} $factor")
    }

    override suspend fun changeDirection() {
        controllerToServerMessageFlow.emit(Signals.SIG_CHANGE_DIRECTION)
    }

    override suspend fun goto(teamId: String, index: Int) {
        controllerToServerMessageFlow.emit("${Signals.SIG_GOTO} $index $teamId")
    }

    override suspend fun authenticate(login: String, password: String) {
        controllerToServerMessageFlow.emit("$login $password")
    }

    override suspend fun sendGetVariantsToGotoSignal(teamId: String) {
        controllerToServerMessageFlow.emit("${Signals.SIG_GET_VARIANTS_TO_GOTO} $teamId")
    }

    override fun getConnectionFailureFlow(): Flow<Unit> = connectionFailureFlow

    override fun getVariantsToGotoFlow(): Flow<ServerToControllerMessage.VariantsToGoto?> =
        variantsToGotoFlow

    override fun getSettingsFlow(): Flow<ServerToControllerMessage.ScoreboardManagerSettings?> =
        settingsFlow

}
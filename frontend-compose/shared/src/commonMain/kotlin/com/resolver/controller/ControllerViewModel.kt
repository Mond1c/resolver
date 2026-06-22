package com.resolver.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resolver.ControllerRepository
import com.resolver.ServerToControllerMessage
import com.resolver.State
import com.resolver.VariantToGoto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ControllerViewModel(
    private val repository: ControllerRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    val variantsToGotoFlow: StateFlow<ServerToControllerMessage.VariantsToGoto?> =
        repository.getVariantsToGotoFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val settingsFlow =
        repository.getSettingsFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isConnectedWithAuth = MutableStateFlow(false)
    val isConnectedWithAuth = _isConnectedWithAuth.asStateFlow()

    init {
        viewModelScope.launch(defaultDispatcher) {
            launch {
                settingsFlow.collect {
                    it?.let {
                        _isConnectedWithAuth.update { true }
                    } ?: _isConnectedWithAuth.update { false }
                }
            }
            launch {
                repository.getConnectionFailureFlow().collect {
                    _isConnectedWithAuth.update { false }
                }
            }
        }
    }

    fun stop() {
        viewModelScope.launch(defaultDispatcher) {
            repository.stop()
        }
    }

    fun start() {
        viewModelScope.launch(defaultDispatcher) {
            repository.start()
        }
    }

    fun up() {
        viewModelScope.launch(defaultDispatcher) {
            repository.up()
        }
    }

    fun down() {
        viewModelScope.launch(defaultDispatcher) {
            repository.down()
        }
    }

    fun applyFactor(factor: Double) {
        viewModelScope.launch(defaultDispatcher) {
            repository.applyFactor(factor)
        }
    }

    fun changeDirection() {
        viewModelScope.launch(defaultDispatcher) {
            repository.changeDirection()
        }
    }

    fun goto(teamId: String, index: Int?) {
        viewModelScope.launch(defaultDispatcher) {
            index?.let {
                repository.goto(teamId, it)
            }
        }
    }

    fun authenticate(login: String, password: String) {
        viewModelScope.launch(defaultDispatcher) {
            repository.authenticate(login, password)
        }
    }

    fun sendGetVariantsToGotoSignal(teamId: String) {
        viewModelScope.launch(defaultDispatcher) {
            repository.sendGetVariantsToGotoSignal(teamId)
        }
    }

    fun getStateString(state: State) = when (state) {
        State.PROCESS -> "in progress"
        State.STOP -> "stopped"
    }

    fun getVariantToGotoString(variantToGoto: VariantToGoto): String {
        return "${variantToGoto.stateIndex}: ${
            variantToGoto.problemsToResolveDisplayNames.joinToString(
                ", "
            )
        }"
    }

}
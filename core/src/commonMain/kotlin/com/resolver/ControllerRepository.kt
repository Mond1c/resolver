package com.resolver

import kotlinx.coroutines.flow.Flow

interface ControllerRepository {
    suspend fun stop()
    suspend fun start()
    suspend fun up()
    suspend fun down()
    suspend fun applyFactor(factor: Double)
    suspend fun changeDirection()
    suspend fun goto(teamId: String, index: Int)
    suspend fun authenticate(login: String, password: String)
    suspend fun sendGetVariantsToGotoSignal(teamId: String)
    fun getConnectionFailureFlow(): Flow<Unit>
    fun getVariantsToGotoFlow(): Flow<ServerToControllerMessage.VariantsToGoto?>
    fun getSettingsFlow(): Flow<ServerToControllerMessage.ScoreboardManagerSettings?>
}
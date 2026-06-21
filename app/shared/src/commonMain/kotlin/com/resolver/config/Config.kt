package com.resolver.config

import Resolver.app.shared.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import resolver.app.shared.generated.resources.Res

@Serializable
internal data class Config(
    val resolutionUrl: String,
    val resolutionControllerUrl: String,
    val resolutionReconnectTimeMs: Long,
    val resolutionControllerReconnectTimeMs: Long
) {
    companion object {
        val DEFAULT by lazy {
            Config(
                resolutionUrl = "ws://localhost:8080/resolution",
                resolutionControllerUrl = "ws://localhost:8080/control",
                resolutionReconnectTimeMs = 3000,
                resolutionControllerReconnectTimeMs = 3000
            )
        }
    }
}

@SingleIn(AppScope::class)
@Inject
internal class ConfigLoader(
    private val json: Json
) {
    private var config: Config? = null
    private val mtx = Mutex()

    suspend fun loadConfig(): Config {
        return config ?: mtx.withLock {
            config ?: load()
        }
    }

    private suspend fun load(): Config {
        val loaded = try {
            json.decodeFromString<Config>(
                Res.readBytes(BuildConfig.CONFIG_PATH).decodeToString()
            )
        } catch (_: Exception) {
            Config.DEFAULT
        }
        config = loaded
        return loaded
    }
}
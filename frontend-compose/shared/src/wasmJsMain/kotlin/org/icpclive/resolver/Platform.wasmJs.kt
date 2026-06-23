package org.icpclive.resolver

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getEngine(): HttpClientEngine = Js.create {

}
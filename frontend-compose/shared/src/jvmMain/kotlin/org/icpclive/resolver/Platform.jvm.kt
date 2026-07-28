package org.icpclive.resolver

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getEngine(): HttpClientEngine = CIO.create {

}
package org.icpclive.resolver.frontend_server

import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.options.option
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal inline val ParameterHolder.endpointOption
    get() = option(
        "-ep", "--endpoint",
        help = "Base path (without leading slash)"
    )

internal fun startServer(
    cmdOptions: BaseCommandLineOptions
) {
    embeddedServer(Netty, port = cmdOptions.port, host = cmdOptions.host) {
        routing {
            get("/") {
                call.respondRedirect("/${cmdOptions.endpoint}")
            }
            get("/{...}") {
                val requestPath = call.request.path()
                val resourcePath = when {
                    requestPath == "/${cmdOptions.endpoint}" || requestPath == "/${cmdOptions.endpoint}/" ->
                        "${cmdOptions.endpoint}/index.html"

                    requestPath.startsWith("/${cmdOptions.endpoint}/") ->
                        requestPath.removePrefix("/")

                    else -> "${cmdOptions.endpoint}${requestPath}"
                }
                provideResource(resourcePath)
            }
        }
    }.start(true)
}

private suspend fun RoutingContext.provideResource(resourcePath: String) {
    val decodedPath = withContext(Dispatchers.IO) {
        URLDecoder.decode(resourcePath, StandardCharsets.UTF_8.name())
    }
    val resource = this::class.java.classLoader.getResource(decodedPath)
    resource?.readBytes()?.let {
        call.respondBytes(it, chooseContentType(decodedPath))
    } ?: call.respond(HttpStatusCode.NotFound)
}

private fun chooseContentType(path: String): ContentType {
    return when {
        path.endsWith(".html") -> ContentType.Text.Html
        path.endsWith(".css") -> ContentType.Text.CSS
        path.endsWith(".js") -> ContentType.Text.JavaScript
        path.endsWith(".svg") -> ContentType.Image.SVG
        path.endsWith(".png") -> ContentType.Image.PNG
        path.endsWith(".json") -> ContentType.Application.Json
        path.endsWith(".ico") -> ContentType.Image.XIcon
        path.endsWith(".wasm") -> ContentType.Application.Wasm
        path.endsWith(".jpeg") || path.endsWith(".jpg") -> ContentType.Image.JPEG
        else -> ContentType.Application.OctetStream
    }
}
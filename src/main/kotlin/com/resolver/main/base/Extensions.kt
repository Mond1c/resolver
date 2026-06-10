package com.resolver.main.base

import com.github.ajalt.clikt.core.ParameterHolder
import com.github.ajalt.clikt.parameters.options.option
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

inline val ParameterHolder.endpointOption
    get() = option(
        "-ep", "--endpoint",
        help = "Base path (without leading slash)"
    )

fun startServer(
    cmdOptions: BaseCommandLineOptions
) {
    embeddedServer(Netty, port = cmdOptions.port, host = cmdOptions.host) {
        routing {
            get("/") {
                call.respondRedirect("/${cmdOptions.endpoint}")
            }
            get("/${cmdOptions.endpoint}/{...}") {
                val requestPath = call.request.path()
                val relativePath = requestPath.removePrefix("/${cmdOptions.endpoint}").removePrefix("/")
                if (relativePath.isBlank()) {
                    "${cmdOptions.endpoint}/index.html"
                } else {
                    "${cmdOptions.endpoint}/$relativePath"
                }.also {
                    provideResource(it)
                }
            }
            get("/assets/{...}") {
                val requestPath = call.request.path()
                val resourcePath = "${cmdOptions.endpoint}$requestPath"
                provideResource(resourcePath)
            }
        }
    }.start(true)
}

private suspend fun RoutingContext.provideResource(resourcePath: String) {
    val decodedPath = URLDecoder.decode(resourcePath, StandardCharsets.UTF_8.name())
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
        path.endsWith(".jpeg") || path.endsWith(".jpg") -> ContentType.Image.JPEG
        else -> ContentType.Application.OctetStream
    }
}
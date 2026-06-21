package com.resolver.util_impl

import com.resolver.util_api.ResolverOptions
import com.resolver.util_api.ResolverOptionsHandler
import com.resolver.util_api.YesNoConsoleHandler
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.system.exitProcess

class ResolverOptionsHandlerImpl(
    private val json: Json,
    private val yesNoConsoleHandler: YesNoConsoleHandler
) : ResolverOptionsHandler {
    override fun handleGenResolverOptionsOption(
        isGenResolverOptionsOptionEnabled: Boolean,
        resolverOptionsPath: Path
    ) {
        if (isGenResolverOptionsOptionEnabled) {
            val yes = if (resolverOptionsPath.exists()) {
                println("Template already exists. Are you sure you want to regenerate resolver options template? [y/n]")
                yesNoConsoleHandler.handleYesNo()
            } else {
                false
            }
            if (yes || !resolverOptionsPath.exists()) {
                resolverOptionsPath.writeText(
                    json.encodeToString<ResolverOptionsImpl>(ResolverOptionsImpl.DEFAULT)
                )
            }
            exitProcess(0)
        }
    }

    override fun getResolverOptions(resolverOptionsPath: Path): ResolverOptions = try {
        resolverOptionsPath
            .readText()
            .let { json.decodeFromString<ResolverOptionsImpl>(it) }
    } catch (_: Exception) {
        ResolverOptionsImpl.DEFAULT
    }
}
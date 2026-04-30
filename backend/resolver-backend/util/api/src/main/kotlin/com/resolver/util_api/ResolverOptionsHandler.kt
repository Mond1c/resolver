package com.resolver.util_api

import java.nio.file.Path

interface ResolverOptionsHandler {
    fun handleGenResolverOptionsOption(
        isGenResolverOptionsOptionEnabled: Boolean,
        resolverOptionsPath: Path
    )

    fun getResolverOptions(resolverOptionsPath: Path): ResolverOptions
}
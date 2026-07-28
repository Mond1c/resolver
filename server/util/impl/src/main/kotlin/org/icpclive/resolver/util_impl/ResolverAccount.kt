package org.icpclive.resolver.util_impl

import kotlinx.serialization.Serializable

@Serializable
internal data class ResolverAccount(
    val login: String,
    val password: String
)
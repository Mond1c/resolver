package org.icpclive.resolver.util_impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.icpclive.resolver.util_api.ResolverAccounts
import org.icpclive.resolver.util_api.ResolverAccountsLoader
import java.nio.file.Path
import kotlin.io.path.readText

@OptIn(ExperimentalSerializationApi::class)
class ResolverAccountsLoaderImpl(
    private val json: Json,
    private val credentialPath: Path?
) : ResolverAccountsLoader {
    override fun loadAccounts(accountsPath: Path): ResolverAccounts {
        val creds: Map<String, String> = credentialPath?.let {
            json.decodeFromStream<Map<String, String>?>(it.toFile().inputStream())
        } ?: emptyMap()
        return ResolverAccountsImpl(
            json.decodeFromString<List<ResolverAccount>>(
                accountsPath.readText()
            )
                .resolveCredentials(creds)
        )
    }
}
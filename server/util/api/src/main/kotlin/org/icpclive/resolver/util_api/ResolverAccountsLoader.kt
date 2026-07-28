package org.icpclive.resolver.util_api

import java.nio.file.Path

interface ResolverAccountsLoader {
    fun loadAccounts(accountsPath: Path): ResolverAccounts
}
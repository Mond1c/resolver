package com.resolver.util_impl

import com.resolver.util_api.ResolverAccounts

@JvmInline
internal value class ResolverAccountsImpl(
    val accounts: List<ResolverAccount>
) : ResolverAccounts {
    override fun contains(login: String?, password: String?): Boolean =
        if (password != null && login != null) {
            accounts.contains(ResolverAccount(login, password))
        } else {
            false
        }
}
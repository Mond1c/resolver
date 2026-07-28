package org.icpclive.resolver.util_api

interface ResolverAccounts {
    fun contains(login: String?, password: String?): Boolean
}
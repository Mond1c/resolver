package com.resolver.util_api

interface ResolverAccounts {
    fun contains(login: String?, password: String?): Boolean
}
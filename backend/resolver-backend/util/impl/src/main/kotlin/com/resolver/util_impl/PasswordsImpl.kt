package com.resolver.util_impl

import com.resolver.util_api.Passwords

@JvmInline
internal value class PasswordsImpl(
    val passwords: List<Password>
) : Passwords {
    override fun contains(password: String?): Boolean = if (password != null) {
        passwords.contains(Password(password))
    } else {
        false
    }
}
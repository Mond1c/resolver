package com.resolver.util_api

import java.nio.file.Path

interface PasswordsLoader {
    fun loadPasswords(passwordsPath: Path): Passwords
}
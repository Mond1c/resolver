package com.resolver.util_impl.exception

import com.resolver.util_impl.Constants

internal class PasswordNotFoundException : RuntimeException() {
    override val message: String = "File ${Constants.RESOLVER_PASSWORDS_FILE_NAME} not found in config directory"
}
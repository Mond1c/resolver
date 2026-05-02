package com.resolver.util_impl

import com.resolver.util_api.Passwords
import com.resolver.util_api.PasswordsLoader
import com.resolver.util_impl.exception.PasswordNotFoundException
import kotlinx.io.files.FileNotFoundException
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText

class PasswordsLoaderImpl(
    private val json: Json
) : PasswordsLoader {
    override fun loadPasswords(passwordsPath: Path): Passwords {
        return try {
            PasswordsImpl(
                json.decodeFromString<List<Password>>(
                    passwordsPath.readText()
                )
            )
        } catch (_: FileNotFoundException) {
            throw PasswordNotFoundException()
        }
    }

}
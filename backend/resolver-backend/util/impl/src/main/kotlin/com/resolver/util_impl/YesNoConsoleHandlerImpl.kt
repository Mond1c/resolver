package com.resolver.util_impl

import com.resolver.util_api.YesNoConsoleHandler

object YesNoConsoleHandlerImpl : YesNoConsoleHandler {
    override fun handleYesNo(): Boolean {
        while (true) {
            when (readln().trim().lowercase()) {
                "y", "yes" -> {
                    return true
                }

                "n", "no" -> {
                    return false
                }

                else -> {}
            }
        }
    }
}
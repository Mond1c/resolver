package com.resolver.resolution_logic_impl

import com.resolver.util_api.exception.InvariantViolationException

internal object ResolverExceptions {
    val contestStatesToApplyIsEmptyException by lazy {
        InvariantViolationException("If contest states to apply is not null then it must be non empty")
    }
}
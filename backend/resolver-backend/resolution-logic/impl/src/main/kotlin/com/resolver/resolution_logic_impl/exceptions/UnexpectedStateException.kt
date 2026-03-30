package com.resolver.resolution_logic_impl.exceptions

internal class UnexpectedStateException(reason: String) :
    IllegalStateException("Unexpected state was reached: $reason")
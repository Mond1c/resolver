package com.resolver

class UnexpectedStateException(reason: String) :
    IllegalStateException("Unexpected state was reached: $reason")
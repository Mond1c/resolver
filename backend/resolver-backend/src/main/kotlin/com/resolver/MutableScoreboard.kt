package com.resolver

interface MutableScoreboard<T : MutableRow> {
    fun getCurrentRow(): T

    fun sort(): ResolutionStep
}
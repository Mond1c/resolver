package com.resolver.resolution_logic_api

interface MutableScoreboard<T : MutableRow> {
    fun getCurrentRow(): T?

    fun sort(problemId: String): ResolutionStep

    fun up()
}
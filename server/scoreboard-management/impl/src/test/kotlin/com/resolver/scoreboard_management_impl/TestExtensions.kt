package com.resolver.scoreboard_management_impl

internal fun <T> List<T>.safeSublist(index: Int, delta: Int = 5): List<T> {
    return subList(maxOf(0, index - delta), minOf(size, index + delta))
}
package com.resolver.resolution_logic_api

sealed interface MutableRow {
    val teamId: String
    var rank: Int
}
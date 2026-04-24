package com.resolver

import com.github.ajalt.clikt.core.main
import com.resolver.util_di.ResolverUtilComponent

fun main(args: Array<String>) = App(
    ResolverUtilComponent.scoreboardCalculator1
).main(args)
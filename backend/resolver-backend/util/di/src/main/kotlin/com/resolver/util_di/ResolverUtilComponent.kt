package com.resolver.util_di

import com.resolver.util_api.ScoreboardCalculator
import com.resolver.util_impl.ScoreboardCalculatorImpl

object ResolverUtilComponent {
    val scoreboardCalculator1: ScoreboardCalculator by lazy {
        ScoreboardCalculatorImpl
    }
}
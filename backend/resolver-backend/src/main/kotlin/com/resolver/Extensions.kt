package com.resolver

import org.icpclive.clics.objects.Contest
import kotlin.time.Duration

fun Contest.calculateFreezeContestStartTime(): Duration {
    return duration - (scoreboardFreezeDuration ?: Duration.ZERO)
}
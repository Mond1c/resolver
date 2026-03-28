package com.resolver.resolution_logic_impl

import org.icpclive.clics.objects.Contest
import kotlin.time.Duration

internal fun Contest.calculateFreezeContestStartTime(): Duration {
    return duration - (scoreboardFreezeDuration ?: Duration.ZERO)
}
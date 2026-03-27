package com.resolver

abstract class EventFeedResolutionPreparator {
    protected val steps = mutableListOf<ResolutionStep>()

    abstract fun prepareResolution()
}
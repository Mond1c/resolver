package com.resolver.resolution_logic_api

abstract class EventFeedResolutionPreparator {
    protected val steps = mutableListOf<ResolutionStep>()

    abstract fun prepareResolution(): List<ResolutionStep>
}
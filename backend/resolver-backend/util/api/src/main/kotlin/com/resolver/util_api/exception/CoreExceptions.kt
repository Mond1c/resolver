package com.resolver.util_api.exception

object CoreExceptions {
    val awardNotFoundException by lazy {
        InvariantViolationException("Map must contain all awards")
    }

    val contestInfoIsNullException by lazy {
        UnexpectedStateException("ContestInfo is null")
    }

    val badUiEventsSequenceException by lazy {
        InvariantViolationException("Only ShowTeamAwards, UnchooseRow and ChooseProblem are possible right after ChooseRow")
    }

    val problemNotFoundException by lazy {
        UnexpectedStateException("Map must contain problem")
    }

    val teamNotFoundException by lazy {
        UnexpectedStateException("Map must contain team")
    }

    val contestStatesIsEmptyException by lazy {
        UnexpectedStateException("List of contest states must be non empty")
    }
}
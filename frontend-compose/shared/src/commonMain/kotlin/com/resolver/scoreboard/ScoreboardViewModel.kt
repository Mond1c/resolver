package com.resolver.scoreboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resolver.ContestResultType
import com.resolver.ScoreboardRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ScoreboardViewModel(
    repository: ScoreboardRepository
) : ViewModel() {
    val scoreboard = repository.getScoreboardFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    val problems = repository.getProblemsFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf())

    val chosenRowTeamId = repository.getChosenRowTeamIdFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val chosenRowIndex = repository.getChosenRowIndexFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val chosenProblem = repository.getChosenProblemFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isConnected = repository.getIsConnectedFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isICPC = repository.getContestInfoFlow()
        .map { it?.resultType == ContestResultType.ICPC }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
}
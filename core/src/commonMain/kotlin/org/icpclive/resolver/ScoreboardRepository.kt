package org.icpclive.resolver

import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

data class ScoreboardRowWithTeamId(
    val teamId: TeamId,
    val name: String,
    val rank: Int,
    val row: ScoreboardRow
)

typealias Scoreboard = PersistentList<ScoreboardRowWithTeamId>

interface ScoreboardRepository {
    fun getIsConnectedFlow(): Flow<Boolean>
    fun getScoreboardFlow(): Flow<Scoreboard>
    fun getProblemsFlow(): Flow<List<ProblemInfo>>
    fun getChosenRowTeamIdFlow(): Flow<TeamId?>
    fun getChosenRowIndexFlow(): Flow<Int?>
    fun getChosenProblemFlow(): Flow<ProblemId?>
    fun getContestInfoFlow(): Flow<ContestInfo?>
}


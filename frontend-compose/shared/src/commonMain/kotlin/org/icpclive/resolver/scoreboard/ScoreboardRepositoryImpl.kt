package org.icpclive.resolver.scoreboard

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.icpclive.resolver.ContestInfo
import org.icpclive.resolver.ProblemId
import org.icpclive.resolver.ProblemInfo
import org.icpclive.resolver.Scoreboard
import org.icpclive.resolver.ScoreboardRepository
import org.icpclive.resolver.ScoreboardRowWithTeamId
import org.icpclive.resolver.TeamId
import org.icpclive.resolver.TeamInfo
import org.icpclive.resolver.UiEvent

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class ScoreboardRepositoryImpl(
    uiEventsFlow: Flow<UiEvent>,
    private val isConnectedFlow: MutableStateFlow<Boolean>,
    scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default +
                CoroutineExceptionHandler { _, _ -> })
) : ScoreboardRepository {
    private val scoreboard = MutableStateFlow<Scoreboard>(persistentListOf())
    private val problems = MutableStateFlow<List<ProblemInfo>>(listOf())
    private val chosenRowTeamId = MutableStateFlow<TeamId?>(null)
    private val chosenRowIndex = MutableStateFlow<Int?>(null)
    private val chosenProblem = MutableStateFlow<ProblemId?>(null)
    private val contestInfo = MutableStateFlow<ContestInfo?>(null)
    private val teamIdToRow = hashMapOf<TeamId, ScoreboardRowWithTeamId>()
    private val teamIdToTeamInfo = mutableMapOf<TeamId, TeamInfo>()

    init {
        scope.launch {
            uiEventsFlow.collect { event ->
                when (event) {
                    is UiEvent.Accept -> {
                        handleAccept(event)
                    }

                    is UiEvent.ChooseProblem -> {
                        handleChooseProblem(event)
                    }

                    is UiEvent.ChooseRow -> {
                        handleChooseRow(event)
                    }

                    is UiEvent.HideGroupAwards -> {}
                    is UiEvent.HideTeamAwards -> {}
                    UiEvent.NoOp -> {}
                    is UiEvent.Reject -> {
                        handleReject(event)
                    }

                    is UiEvent.ReverseAccept -> {
                        handleAccept(event)
                    }

                    is UiEvent.ReverseReject -> {
                        handleReject(event)
                    }

                    is UiEvent.Scoreboard -> {
                        handleScoreboard(event)
                    }

                    is UiEvent.ShowGroupAwards -> {}
                    is UiEvent.ShowTeamAwards -> {}
                    is UiEvent.UnchooseProblem -> {
                        handleUnchooseProblem(event)
                    }

                    is UiEvent.UnchooseRow -> {
                        handleUnchooseRow(event)
                    }
                }
            }
        }
    }

    override fun getIsConnectedFlow(): Flow<Boolean> = isConnectedFlow

    override fun getScoreboardFlow(): Flow<Scoreboard> = scoreboard

    override fun getProblemsFlow(): Flow<List<ProblemInfo>> = problems

    override fun getChosenRowTeamIdFlow(): Flow<TeamId?> = chosenRowTeamId

    override fun getChosenRowIndexFlow(): Flow<Int?> = chosenRowIndex

    override fun getChosenProblemFlow(): Flow<ProblemId?> = chosenProblem
    override fun getContestInfoFlow(): Flow<ContestInfo?> = contestInfo

    private fun handleAccept(event: UiEvent.AcceptOrReverse) {
        var newScoreboard = persistentListOf<ScoreboardRowWithTeamId>()
        event.order.forEachIndexed { index, teamId ->
            val teamInfo = teamIdToTeamInfo[teamId]!!
            if (teamId == event.teamId) {
                teamIdToRow[teamId] = ScoreboardRowWithTeamId(
                    teamId = teamId,
                    name = teamInfo.displayName,
                    rank = event.ranks[index],
                    row = event.row
                ).also {
                    newScoreboard = newScoreboard.adding(it)
                }
            } else {
                val row = teamIdToRow[teamId]!!
                if (event.ranks[index] == row.rank) {
                    newScoreboard = newScoreboard.adding(row)
                } else {
                    val new = row.copy(rank = event.ranks[index])
                    teamIdToRow[teamId] = new
                    newScoreboard = newScoreboard.adding(new)
                }
            }
        }
        scoreboard.update {
            newScoreboard
        }
    }

    private fun handleReject(event: UiEvent.RejectOrReverse) {
        var newScoreboard = scoreboard.value
        newScoreboard.forEachIndexed { index, row ->
            if (event.teamId == row.teamId) {
                teamIdToRow[event.teamId] = row.copy(
                    row = event.row
                ).also {
                    newScoreboard = newScoreboard.replacingAt(index, it)
                }
            }
        }
        scoreboard.update {
            newScoreboard
        }
    }

    private fun handleChooseProblem(event: UiEvent.ChooseProblem) {
        chosenProblem.update {
            event.problemId
        }
    }

    private fun handleChooseRow(event: UiEvent.ChooseRow) {
        chosenRowTeamId.update {
            event.teamId
        }
        chosenRowIndex.update {
            event.index
        }
    }

    private fun handleUnchooseProblem(event: UiEvent.UnchooseProblem) {
        chosenProblem.update {
            null
        }
    }

    private fun handleUnchooseRow(event: UiEvent.UnchooseRow) {
        chosenRowTeamId.update {
            null
        }
        chosenRowIndex.update { event.index }
    }

    private fun handleScoreboard(event: UiEvent.Scoreboard) {
        problems.update {
            event.contestInfo.problemList
        }
        contestInfo.update {
            event.contestInfo
        }
        teamIdToTeamInfo.clear()
        teamIdToRow.clear()
        teamIdToTeamInfo.putAll(event.contestInfo.teamList.associateBy { it.id })
        var newScoreboard = persistentListOf<ScoreboardRowWithTeamId>()
        event.order.forEachIndexed { index, teamId ->
            newScoreboard = newScoreboard.adding(
                ScoreboardRowWithTeamId(
                    teamId = teamId,
                    name = teamIdToTeamInfo[teamId]!!.displayName,
                    rank = event.ranks[index],
                    row = event.teamIdToScoreboardRow[teamId]!!
                ).also {
                    teamIdToRow[it.teamId] = it
                }
            )
        }
        scoreboard.update {
            newScoreboard
        }
        if (event.isLastChosenRowChosenNow) {
            chosenRowTeamId.update {
                event.teamOfLastChosenRow
            }
        } else {
            chosenRowTeamId.update {
                null
            }
        }
        chosenRowIndex.update {
            event.indexOfLastChosenRow
        }
    }
}
package org.icpclive.resolver.scoreboard_management_api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.icpclive.cds.api.Award
import org.icpclive.cds.api.ContestInfo
import org.icpclive.cds.api.ContestResultType
import org.icpclive.cds.api.GroupId
import org.icpclive.cds.api.ICPCProblemResult
import org.icpclive.cds.api.IOIProblemResult
import org.icpclive.cds.api.InefficientContestInfoApi
import org.icpclive.cds.api.OrganizationId
import org.icpclive.cds.api.OrganizationInfo
import org.icpclive.cds.api.ProblemId
import org.icpclive.cds.api.ProblemInfo
import org.icpclive.cds.api.ProblemResult
import org.icpclive.cds.api.ScoreboardRow
import org.icpclive.cds.api.TeamId
import org.icpclive.cds.api.TeamInfo
import org.icpclive.resolver.ProblemResult as ProblemResultCore
import org.icpclive.resolver.ScoreboardRow as ScoreboardRowCore
import org.icpclive.resolver.UiEvent as UiEventCore

@Serializable
sealed interface UiEvent {
    @Serializable
    @SerialName("ChooseRow")
    data class ChooseRow(
        val index: Int,
        val teamId: TeamId
    ) : UiEvent

    @Serializable
    @SerialName("UnchooseRow")
    data class UnchooseRow(
        val index: Int,
        val teamId: TeamId
    ) : UiEvent

    @Serializable
    @SerialName("ChooseProblem")
    data class ChooseProblem(
        val index: Int,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("UnchooseProblem")
    data class UnchooseProblem(
        val index: Int,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("Accept")
    data class Accept(
        val row: ScoreboardRow,
        val ranks: List<Int>,
        val order: List<TeamId>,
        @Transient val oldRow: ScoreboardRow? = null,
        @Transient val oldRanks: List<Int>? = null,
        @Transient val oldOrder: List<TeamId>? = null,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ReverseAccept")
    data class ReverseAccept(
        val oldRow: ScoreboardRow,
        val oldRanks: List<Int>,
        val oldOrder: List<TeamId>,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("Reject")
    data class Reject(
        val row: ScoreboardRow,
        @Transient val oldRow: ScoreboardRow? = null,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ReverseReject")
    data class ReverseReject(
        val oldRow: ScoreboardRow,
        val teamId: TeamId,
        val problemId: ProblemId
    ) : UiEvent

    @Serializable
    @SerialName("ShowTeamAwards")
    data class ShowTeamAwards(
        val teamId: TeamId,
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("HideTeamAwards")
    data class HideTeamAwards(
        val teamId: TeamId,
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("ShowGroupAwards")
    data class ShowGroupAwards(
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("HideGroupAwards")
    data class HideGroupAwards(
        val awards: List<Award>
    ) : UiEvent

    @Serializable
    @SerialName("Scoreboard")
    class Scoreboard(
        val teamIdToScoreboardRow: Map<TeamId, ScoreboardRow>,
        val order: List<TeamId>,
        val ranks: List<Int>,
        val contestInfo: ContestInfo,
        val indexOfLastChosenRow: Int?,
        val teamOfLastChosenRow: TeamId?,
        val isLastChosenRowChosenNow: Boolean
    ) : UiEvent

    @Serializable
    @SerialName("NoOp")
    object NoOp : UiEvent
}

fun ProblemResult.toCore(): ProblemResultCore = when (this) {
    is ICPCProblemResult -> org.icpclive.resolver.ICPCProblemResult(
        wrongAttempts = wrongAttempts,
        pendingAttempts = pendingAttempts,
        isSolved = isSolved,
        isFirstToSolve = isFirstToSolve,
        lastSubmitTime = lastSubmitTime
    )

    is IOIProblemResult -> org.icpclive.resolver.IOIProblemResult(
        score = score,
        lastSubmitTime = lastSubmitTime,
        isFirstBest = isFirstBest,
        pendingAttempts = pendingAttempts,
        totalAttempts = totalAttempts
    )
}

fun TeamId.toCore() = org.icpclive.resolver.TeamId(value)
fun Collection<TeamId>.toCore() = map(TeamId::toCore)
fun OrganizationId.toCore() = org.icpclive.resolver.OrganizationId(value)
fun GroupId.toCore() = org.icpclive.resolver.GroupId(value)
fun ProblemId.toCore() = org.icpclive.resolver.ProblemId(value)

fun Award.toCore() = when (this) {
    is Award.Custom -> org.icpclive.resolver.Award.Custom(
        id = id,
        citation = citation,
        teams = teams.toCore().toSet()
    )

    is Award.GroupChampion -> org.icpclive.resolver.Award.GroupChampion(
        id = id,
        citation = citation,
        groupId = groupId.toCore(),
        teams = teams.toCore().toSet()
    )

    is Award.Medal -> org.icpclive.resolver.Award.Medal(
        id = id,
        citation = citation,
        teams = teams.toCore().toSet()
    )

    is Award.Winner -> org.icpclive.resolver.Award.Winner(
        id = id,
        citation = citation,
        teams = teams.toCore().toSet()
    )
}

fun ContestResultType.toCore() = when (this) {
    ContestResultType.ICPC -> org.icpclive.resolver.ContestResultType.ICPC
    ContestResultType.IOI -> org.icpclive.resolver.ContestResultType.IOI
}

fun ProblemInfo.toCore() = org.icpclive.resolver.ProblemInfo(
    id = id.toCore(),
    displayName = displayName,
    fullName = fullName,
    ordinal = ordinal,
    isHidden = isHidden
)

fun TeamInfo.toCore() = org.icpclive.resolver.TeamInfo(
    id = id.toCore(),
    fullName = fullName,
    displayName = displayName,
    groups = groups.map { it.toCore() },
    hashTag = hashTag,
    organizationId = organizationId?.toCore()
)

fun OrganizationInfo.toCore() = org.icpclive.resolver.OrganizationInfo(
    id = id.toCore(),
    displayName = displayName,
    fullName = fullName
)

fun List<Award>.toCore() = map(Award::toCore)

@OptIn(InefficientContestInfoApi::class)
fun ContestInfo.toCore() = org.icpclive.resolver.ContestInfo(
    name = name,
    resultType = resultType.toCore(),
    problemList = problemList.map { it.toCore() },
    teamList = teamList.map { it.toCore() },
    organizationList = organizationList.map { it.toCore() }
)

fun ScoreboardRow.toCore(): ScoreboardRowCore = ScoreboardRowCore(
    totalScore = totalScore,
    penalty = penalty,
    lastAccepted = lastAccepted,
    problemResults = problemResults.map { it.toCore() }
)

fun UiEvent.toCore(): UiEventCore = when (this) {
    is UiEvent.Accept -> UiEventCore.Accept(
        row = row.toCore(),
        ranks = ranks,
        order = order.toCore(),
        teamId = teamId.toCore(),
        problemId = problemId.toCore()
    )

    is UiEvent.ChooseProblem -> UiEventCore.ChooseProblem(
        index = index,
        teamId = teamId.toCore(),
        problemId = problemId.toCore()
    )

    is UiEvent.ChooseRow -> UiEventCore.ChooseRow(
        index = index,
        teamId = teamId.toCore()
    )

    is UiEvent.HideGroupAwards -> UiEventCore.HideGroupAwards(
        awards = awards.toCore()
    )

    is UiEvent.HideTeamAwards -> UiEventCore.HideTeamAwards(
        teamId = teamId.toCore(),
        awards = awards.toCore()
    )

    UiEvent.NoOp -> UiEventCore.NoOp
    is UiEvent.Reject -> UiEventCore.Reject(
        row = row.toCore(),
        teamId = teamId.toCore(),
        problemId = problemId.toCore()
    )

    is UiEvent.ReverseAccept -> UiEventCore.ReverseAccept(
        row = oldRow.toCore(),
        ranks = oldRanks,
        order = oldOrder.toCore(),
        teamId = teamId.toCore(),
        problemId = problemId.toCore()
    )

    is UiEvent.ReverseReject -> UiEventCore.ReverseReject(
        row = oldRow.toCore(),
        teamId = teamId.toCore(),
        problemId = problemId.toCore()
    )

    is UiEvent.Scoreboard -> UiEventCore.Scoreboard(
        teamIdToScoreboardRow = teamIdToScoreboardRow.mapKeys { it.key.toCore() }
            .mapValues { it.value.toCore() },
        order = order.toCore(),
        ranks = ranks,
        contestInfo = contestInfo.toCore(),
        indexOfLastChosenRow = indexOfLastChosenRow,
        teamOfLastChosenRow = teamOfLastChosenRow?.toCore(),
        isLastChosenRowChosenNow = isLastChosenRowChosenNow
    )

    is UiEvent.ShowGroupAwards -> UiEventCore.ShowGroupAwards(
        awards = awards.toCore()
    )

    is UiEvent.ShowTeamAwards -> UiEventCore.ShowTeamAwards(
        teamId = teamId.toCore(),
        awards = awards.toCore()
    )

    is UiEvent.UnchooseProblem -> UiEventCore.UnchooseProblem(
        index = index,
        teamId = teamId.toCore(),
        problemId = problemId.toCore()
    )

    is UiEvent.UnchooseRow -> UiEventCore.UnchooseRow(
        index = index,
        teamId = teamId.toCore()
    )
}
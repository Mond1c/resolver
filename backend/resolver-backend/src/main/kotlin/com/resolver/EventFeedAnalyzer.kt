package com.resolver

import org.icpclive.clics.objects.*
import java.nio.file.Path

interface EventFeedAnalyzer {
    val contest: Contest
    val judgements: List<Judgement>
    val solvedJudgementTypeIds: Set<String>
    val penaltyJudgementTypeIds: Set<String>
    val submissions: List<Submission>
    val organizations: List<Organization>
    val teams: List<Team>
    val awards: List<Award>

    fun analyze(eventFeedPath: Path)

    fun reset()

    fun resetAndAnalyze(eventFeedPath: Path) {
        reset()
        analyze(eventFeedPath)
    }

    fun filterHiddenTeams()

    fun filterUnjudgedSubmissions()

    fun getProblemIdToFirstSolvedTeamId(): Map<String, String>

    fun getSubmissionIdToJudgements(): Map<String, List<Judgement>>
}
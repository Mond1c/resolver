package com.resolver.resolution_logic_impl.event_feed_analyzer

import com.resolver.resolution_logic_api.EventFeedAnalyzer
import com.resolver.resolution_logic_impl.constants.Constants
import com.resolver.resolution_logic_impl.constants.EventFeedElementTypes
import com.resolver.resolution_logic_impl.exceptions.UnexpectedStateException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.icpclive.clics.objects.*
import java.nio.file.Files
import java.nio.file.Path

internal class EventFeedAnalyzerImpl(
    private val json: Json
) : EventFeedAnalyzer {
    private lateinit var _contest: Contest
    override val contest: Contest
        get() = _contest

    private val _judgements = mutableListOf<Judgement>()
    override val judgements: List<Judgement>
        get() = _judgements

    private val _solvedJudgementTypeIds = hashSetOf<String>()
    override val solvedJudgementTypeIds: Set<String>
        get() = _solvedJudgementTypeIds

    private val _penaltyJudgementTypeIds = hashSetOf<String>()
    override val penaltyJudgementTypeIds: Set<String>
        get() = _penaltyJudgementTypeIds

    private val _submissions = mutableListOf<Submission>()
    override val submissions: List<Submission>
        get() = _submissions

    private val _organizations = mutableListOf<Organization>()
    override val organizations: List<Organization>
        get() = _organizations

    private val _teams = mutableListOf<Team>()
    override val teams: List<Team>
        get() = _teams

    private val _awards = mutableListOf<Award>()
    override val awards: List<Award>
        get() = _awards

    override fun analyze(eventFeedPath: Path) {
        read(eventFeedPath)
    }

    override fun reset() {
        _judgements.clear()
        _solvedJudgementTypeIds.clear()
        _penaltyJudgementTypeIds.clear()
        _submissions.clear()
        _organizations.clear()
        _teams.clear()
        _awards.clear()
    }

    private fun read(eventFeedPath: Path) {
        Files.newBufferedReader(eventFeedPath).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank() || line.startsWith(Constants.JSON_COMMENTARY)) {
                    continue
                }
                val jsonElement = json.parseToJsonElement(line)
                jsonElement.jsonObject[Constants.DATA]?.let { jsonDataElement ->
                    when (jsonElement.jsonObject[Constants.TYPE]?.jsonPrimitive?.content) {
                        null -> {}

                        EventFeedElementTypes.CONTEST -> {
                            _contest =
                                json.decodeFromJsonElement<Contest>(jsonDataElement)
                        }

                        EventFeedElementTypes.TEAMS -> {
                            _teams.add(json.decodeFromJsonElement<Team>(jsonDataElement))
                        }

                        EventFeedElementTypes.AWARDS -> {
                            _awards.add(json.decodeFromJsonElement<Award>(jsonDataElement))
                        }

                        EventFeedElementTypes.ORGANIZATIONS -> {
                            _organizations.add(json.decodeFromJsonElement<Organization>(jsonDataElement))
                        }

                        EventFeedElementTypes.JUDGEMENTS -> {
                            _judgements.add(json.decodeFromJsonElement<Judgement>(jsonDataElement))
                        }

                        EventFeedElementTypes.SUBMISSIONS -> {
                            _submissions.add(json.decodeFromJsonElement<Submission>(jsonDataElement))
                        }

                        EventFeedElementTypes.JUDGEMENT_TYPES -> {
                            val judgementType = json.decodeFromJsonElement<JudgementType>(jsonDataElement)
                            if (judgementType.penalty) {
                                _penaltyJudgementTypeIds.add(judgementType.id)
                            }
                            if (judgementType.solved) {
                                _solvedJudgementTypeIds.add(judgementType.id)
                            }
                        }
                    }
                }
            }
        }
        if (!::_contest.isInitialized) {
            throw UnexpectedStateException("Contest must be presented in event feed")
        }
    }

    override fun filterHiddenTeams() {
        val hiddenTeamIds = _teams
            .filter { it.hidden ?: false }
            .map { it.id }
            .toHashSet()
        _awards.forEachIndexed { i, award ->
            _awards[i] = award.copy(teamIds = award.teamIds.filter { it !in hiddenTeamIds })
        }
        _teams.removeIf { it.id in hiddenTeamIds }
        val submissionIdsOfHiddenTeams = _submissions
            .filter { it.teamId in hiddenTeamIds }
            .map { it.id }
            .toHashSet()
        _submissions.removeIf { it.id in submissionIdsOfHiddenTeams }
        _judgements.removeIf { it.submissionId in submissionIdsOfHiddenTeams }
    }

    override fun filterUnjudgedSubmissions() {
        val judgedSubmissionsIds = _judgements
            .map { it.submissionId }
            .toHashSet()
        _submissions.removeIf { it.id !in judgedSubmissionsIds }
        _judgements.removeIf { it.submissionId !in judgedSubmissionsIds }
    }

    override fun getProblemIdToFirstSolvedTeamId(): Map<String, String> {
        val submissionById = _submissions.associateBy { it.id }
        val problemIdToFirstSolvedTeamId = HashMap<String, String>()
        for (judgement in _judgements
            .filter { it.judgementTypeId in solvedJudgementTypeIds }
            .sortedBy { it.endContestTime ?: TODO("Interesting case") }) {
            val submission = submissionById[judgement.submissionId]
                ?: throw UnexpectedStateException("Impossible state reached")
            val firstSolvedTeamId = problemIdToFirstSolvedTeamId[submission.problemId]
            if (firstSolvedTeamId == null) {
                problemIdToFirstSolvedTeamId[submission.problemId] = submission.teamId
            }
        }
        return problemIdToFirstSolvedTeamId
    }

    override fun getSubmissionIdToJudgements(): Map<String, List<Judgement>> {
        return _judgements
            .groupBy { it.submissionId }
            .mapValues { judgement ->
                listOf(
                    judgement.value
                        .filter { it.current ?: true }
                        .sortedBy { it.endTime }
                        .last()
                )
            }
    }
}
package com.resolver

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.icpclive.clics.objects.*
import java.nio.file.Files
import java.nio.file.Path

class EventFeedAnalyzerImpl(
    private val json: Json
) : EventFeedAnalyzer {
    private lateinit var _contest: Contest
    override val contest: Contest
        get() = _contest

    private val _judgements = mutableListOf<Judgement>()
    override val judgements: List<Judgement>
        get() = _judgements

    private val _solvedJudgementTypes = hashSetOf<String>()
    override val solvedJudgementTypeIds: Set<String>
        get() = _solvedJudgementTypes

    private val _penaltyJudgementTypes = hashSetOf<String>()
    override val penaltyJudgementTypeIds: Set<String>
        get() = _penaltyJudgementTypes

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
        _solvedJudgementTypes.clear()
        _penaltyJudgementTypes.clear()
        _submissions.clear()
        _organizations.clear()
        _teams.clear()
    }

    private fun read(eventFeedPath: Path) {
        Files.newBufferedReader(eventFeedPath).use { reader ->
            reader.forEachLine { line ->
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
                                _penaltyJudgementTypes.add(judgementType.id)
                            }
                            if (judgementType.solved) {
                                _solvedJudgementTypes.add(judgementType.id)
                            }
                        }
                    }
                }
            }
        }
    }
}
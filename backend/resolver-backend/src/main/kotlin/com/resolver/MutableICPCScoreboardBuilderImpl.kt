package com.resolver

import kotlin.time.Duration

class MutableICPCScoreboardBuilderImpl : MutableScoreboardBuilder<MutableICPCRow> {
    override fun build(eventFeedAnalyzer: EventFeedAnalyzer): MutableScoreboard<MutableICPCRow> {
        val submissionIdToJudgement = eventFeedAnalyzer
            .judgements
            .groupBy { it.submissionId }
            .mapValues { judgement ->
                judgement.value
                    .filter { it.current ?: true }
            }
        val penaltyTime = eventFeedAnalyzer.contest.penaltyTime
            ?: throw UnexpectedStateException("Penalty time is expected to be defined in pass-fail contests")
        val rows: MutableList<MutableICPCRow> = eventFeedAnalyzer
            .submissions
            .groupBy { it.teamId }
            .mapValues { submissions1 ->
                var totalPenaltyTime = Duration.ZERO // TODO
                var solvedCount = 0 // TODO
                val problemIdToSubmissionsResult: HashMap<String, ICPCSubmissionsResult> = HashMap(
                    submissions1.value
                        .groupBy { submission ->
                            submission.problemId
                        }
                        .mapValues { submissions2 ->
                            var penalty = Duration.ZERO
                            var isSolved = false
                            var isPending = false // TODO
                            for (submission in submissions2.value
                                .sortedBy { it.time }) {
                                val judgement = (submissionIdToJudgement[submission.id]
                                    ?: throw UnexpectedStateException("submissionId=${submission.id} is expected to be the key of map, but it is not"))[0]
                                if (judgement.id in eventFeedAnalyzer.penaltyJudgementTypeIds) {
                                    penalty += penaltyTime
                                } else if (judgement.id in eventFeedAnalyzer.solvedJudgementTypeIds) {
                                    penalty += submission.contestTime
                                    isSolved = true
                                    break
                                }
                            }
                            ICPCSubmissionsResultImpl(
                                penaltyTime = penalty,
                                isSolved = isSolved,
                                isPending = isPending
                            )
                        })
                MutableICPCRowImpl(
                    teamId = submissions1.key,
                    rank = 0,
                    totalPenaltyTime = totalPenaltyTime,
                    solvedCount = solvedCount,
                    problemIdToSubmissionsResult = problemIdToSubmissionsResult
                )
            }
            .values
            .toMutableList()
        return MutableICPCScoreboardImpl(
            rows = rows
        )
    }
}
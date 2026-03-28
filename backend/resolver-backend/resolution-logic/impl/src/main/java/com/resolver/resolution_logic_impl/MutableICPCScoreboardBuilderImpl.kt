package com.resolver.resolution_logic_impl

import com.resolver.resolution_logic_api.*
import kotlin.time.Duration

internal object MutableICPCScoreboardBuilderImpl : MutableScoreboardBuilder<MutableICPCRow> {
    override fun build(eventFeedAnalyzer: EventFeedAnalyzer): MutableScoreboard<MutableICPCRow> {
        val submissionIdToJudgements = eventFeedAnalyzer.getSubmissionIdToJudgements()
        val penaltyTime = eventFeedAnalyzer.contest.penaltyTime
            ?: throw UnexpectedStateException("Penalty time is expected to be defined in pass-fail contests")
        val freezeContestStartTime = eventFeedAnalyzer.contest.calculateFreezeContestStartTime()
        val problemIdToFirstSolvedTeamId = eventFeedAnalyzer.getProblemIdToFirstSolvedTeamId()
        val rows: MutableList<MutableICPCRow> = eventFeedAnalyzer
            .submissions
            .groupBy { it.teamId }
            .mapValues { submissions1 ->
                var totalPenaltyTime = Duration.ZERO
                var solvedCount = 0
                val problemIdToSubmissionsResult: HashMap<String, ICPCSubmissionsResult> = HashMap(
                    submissions1.value
                        .groupBy { submission ->
                            submission.problemId
                        }
                        .mapValues { submissions2 ->
                            var penalty = Duration.ZERO
                            var isSolved = false
                            var isPending = false
                            for (submission in submissions2.value
                                .sortedBy { it.time }) {
                                val judgement = (submissionIdToJudgements[submission.id]
                                    ?: throw UnexpectedStateException("submissionId=${submission.id} is expected to be the key of map, but it is not"))[0]
                                val judgementContestEndTime =
                                    judgement.endContestTime
                                        ?: TODO("Interesting case: judgement does not have end contest time. \n $judgement")
                                if (judgement.id in eventFeedAnalyzer.penaltyJudgementTypeIds) {
                                    if (judgementContestEndTime >= freezeContestStartTime) {
                                        isPending = true
                                    }
                                    penalty += penaltyTime
                                } else if (judgement.id in eventFeedAnalyzer.solvedJudgementTypeIds) {
                                    penalty += submission.contestTime
                                    isSolved = true
                                    if (judgementContestEndTime >= freezeContestStartTime) {
                                        isPending = true
                                    } else {
                                        totalPenaltyTime += penalty
                                        solvedCount++
                                    }
                                    break
                                }
                            }
                            val anySubmission = submissions2.value.firstOrNull()
                                ?: throw UnexpectedStateException("Submissions must have at least one submission")
                            ICPCSubmissionsResultImpl(
                                isFirstToSolve = problemIdToFirstSolvedTeamId[anySubmission.problemId]
                                        == anySubmission.teamId,
                                penaltyTime = penalty,
                                isSolved = isSolved,
                                isPending = isPending,
                                isOpened = !isPending
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
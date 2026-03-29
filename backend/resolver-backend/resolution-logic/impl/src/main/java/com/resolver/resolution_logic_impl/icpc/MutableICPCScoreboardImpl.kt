package com.resolver.resolution_logic_impl.icpc

import com.resolver.resolution_logic_api.MutableICPCRow
import com.resolver.resolution_logic_api.MutableScoreboard
import com.resolver.resolution_logic_api.ResolutionStep
import com.resolver.resolution_logic_impl.exceptions.UnexpectedStateException

internal data class MutableICPCScoreboardImpl(
    private val rows: MutableList<MutableICPCRow>
) : MutableScoreboard<MutableICPCRow> {
    init {
        rows
            .sortWith(
                compareByDescending<MutableICPCRow> { it.solvedCount }
                    .thenBy { it.totalPenaltyTime }
            )
        rows.forEachIndexed { i, row ->
            row.rank = i + 1
        }
    }

    private var currentRowIndex = rows.size - 1

    override fun getCurrentRow(): MutableICPCRow? {
        return rows.getOrNull(currentRowIndex)
    }

    override fun sort(problemId: String): ResolutionStep {
        val submissionsResult = rows[currentRowIndex].problemIdToSubmissionsResult[problemId]
            ?: throw UnexpectedStateException(
                "problemId=$problemId is expected to be the key of map, but it is not"
            )
        if (!submissionsResult.isSolved) {
            return ResolutionStep.RejectResolutionStep(
                rows[currentRowIndex].teamId,
                problemId
            )
        }
        var aboveIndex = currentRowIndex - 1
        val oldIndex = currentRowIndex
        var newIndex = currentRowIndex
        while (aboveIndex >= 0 && rows[newIndex].isBetterThan(rows[aboveIndex])) {
            rows[newIndex] = rows[aboveIndex].also { rows[aboveIndex] = rows[newIndex] }
            rows[newIndex].rank = newIndex + 1
            rows[aboveIndex].rank = aboveIndex + 1
            aboveIndex--
            newIndex--
        }
        return ResolutionStep.ICPCAcceptResolutionStep(
            teamId = rows[newIndex].teamId,
            problemId = problemId,
            oldRank = rows[oldIndex].rank,
            newRank = rows[newIndex].rank,
            oldTotalPenaltyTime = rows[oldIndex].totalPenaltyTime,
            newTotalPenaltyTime = rows[newIndex].totalPenaltyTime,
            isSolved = true,
            isFirstToSolve = submissionsResult.isFirstToSolve,
        )
    }

    override fun up() {
        currentRowIndex--
    }
}
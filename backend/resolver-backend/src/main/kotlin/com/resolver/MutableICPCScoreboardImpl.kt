package com.resolver

class MutableICPCScoreboardImpl(
    private val rows: MutableList<MutableICPCRow>
) : MutableScoreboard<MutableICPCRow> {
    private var currentRowIndex = rows.size - 1

    override fun getCurrentRow(): MutableICPCRow {
        return rows[currentRowIndex]
    }

    override fun sort(): ResolutionStep {
        TODO("Not yet implemented")
    }
}
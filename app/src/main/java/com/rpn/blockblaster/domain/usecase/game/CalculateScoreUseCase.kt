package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.BlastResult
import com.rpn.blockblaster.domain.model.Block

class CalculateScoreUseCase {

    fun cellsPlaced(block: Block): Int = block.cellCount

    fun blastScore(result: BlastResult, comboStreak: Int): Int {
        if (result.rows.isEmpty() && result.cols.isEmpty()) return 0
        val comboMult = 1f + (comboStreak * 0.2f)
        return (result.pointsAwarded * comboMult).toInt()
    }

    fun newComboStreak(result: BlastResult, current: Int): Int =
        if (result.rows.isEmpty() && result.cols.isEmpty()) 0 else current + 1

    fun comboMultiplier(streak: Int): Float = 1f + (streak * 0.2f)
}

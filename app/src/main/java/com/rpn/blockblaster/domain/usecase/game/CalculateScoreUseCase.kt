package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.BlastResult
import com.rpn.blockblaster.domain.model.Block

class CalculateScoreUseCase {

    fun cellsPlaced(block: Block): Int = block.cellCount

    fun blastScore(result: BlastResult, comboStreak: Int): Int {
        if (result.rows.isEmpty() && result.cols.isEmpty()) return 0
        
        // Base points from the blast result
        var points = result.pointsAwarded.toFloat()
        
        // Perfect Clear Bonus: 10x the points if the board is fully cleared!
        // This is a massive motivator for players.
        if (result.isPerfectClear) {
            points *= 10f
        }
        
        // Combo Multiplier: Each streak adds 25% bonus for more intensity
        val comboMult = 1f + (comboStreak * 0.25f)
        
        return (points * comboMult).toInt()
    }

    fun newComboStreak(result: BlastResult, current: Int): Int =
        if (result.rows.isEmpty() && result.cols.isEmpty()) 0 else current + 1

    fun comboMultiplier(streak: Int): Float = 1f + (streak * 0.25f)
}

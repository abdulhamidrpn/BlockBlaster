package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.engine.Difficulty
import com.rpn.blockblaster.domain.model.BlastResult
import com.rpn.blockblaster.domain.model.Block

class CalculateScoreUseCase {

    fun cellsPlaced(block: Block): Int = block.cellCount

    fun blastScore(result: BlastResult, comboStreak: Int, difficulty: Difficulty = Difficulty.MEDIUM): Int {
        if (result.rows.isEmpty() && result.cols.isEmpty()) return 0
        
        // Base points from the blast result
        var points = result.pointsAwarded.toFloat()
        
        // Difficulty Multiplier
        val diffMult = when (difficulty) {
            Difficulty.EASY   -> 1.0f
            Difficulty.MEDIUM -> 1.5f
            Difficulty.HARD   -> 2.0f
        }
        points *= diffMult

        // Perfect Clear Bonus: 15x the points if the board is fully cleared!
        if (result.isPerfectClear) {
            points *= 15f
        }
        
        // Cross Blast Bonus: extra 2x multiplier for cross blasts
        if (result.isCrossBlast) {
            points *= 2f
        }

        // Combo Multiplier: Each streak adds 30% bonus for more intensity
        val comboMult = 1f + (comboStreak * 0.30f)
        
        return (points * comboMult).toInt()
    }

    fun newComboStreak(result: BlastResult, current: Int): Int =
        if (result.rows.isEmpty() && result.cols.isEmpty()) 0 else current + 1

    fun comboMultiplier(streak: Int): Float = 1f + (streak * 0.30f)
}

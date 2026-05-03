package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.engine.BlockShapeFactory
import com.rpn.blockblaster.domain.engine.Difficulty
import com.rpn.blockblaster.domain.model.Block
import com.rpn.blockblaster.domain.model.BoardCell

class SpawnBlocksUseCase(private val placeBlockUseCase: PlaceBlockUseCase) {
    /**
     * Spawns a set of blocks adaptively based on the board's state and current score.
     * Evaluates empty spaces and enforces that at least some blocks are placeable.
     */
    operator fun invoke(
        board: List<List<BoardCell>>,
        difficulty: Difficulty = Difficulty.MEDIUM
    ): List<Block?> {
        val totalCells = board.size * board[0].size
        val emptyCells = board.sumOf { row -> row.count { !it.isFilled } }
        val emptyRatio = emptyCells.toFloat() / totalCells
        
        var bestBatch: List<Block?> = emptyList()
        var maxFittingCount = -1

        // We try pulling up to 10 distinct random batches to find one where blocks fit well.
        for (attempt in 1..10) {
            // Adaptive difficulty: if the board is getting full, temporarily lower the difficulty for this spawn
            val effectiveDifficulty = when {
                emptyRatio < 0.25f && difficulty == Difficulty.HARD   -> Difficulty.MEDIUM
                emptyRatio < 0.15f && difficulty == Difficulty.MEDIUM -> Difficulty.EASY
                emptyRatio < 0.10f -> Difficulty.EASY
                else -> difficulty
            }

            val batch = BlockShapeFactory.spawnBlocks(count = 3, difficulty = effectiveDifficulty).map { it as Block? }
            
            // Re-roll to ensure fairness: evaluate how many blocks from this batch can physically be placed
            var fittingCount = 0
            for (block in batch) {
                if (block != null && canPlaceAnywhere(board, block)) {
                    fittingCount++
                }
            }
            
            // If we found a batch where all 3 fit, perfection! Break early.
            if (fittingCount == 3) {
                return batch
            }
            
            // If the board is extremely full (< 20% empty), we'll gracefully accept even 1 fitting block.
            if (emptyRatio < 0.2f && fittingCount >= 1) {
                return batch
            }
            
            // Otherwise, keep searching for the best possible batch.
            if (fittingCount > maxFittingCount) {
                maxFittingCount = fittingCount
                bestBatch = batch
            }
        }
        
        // Return the best batch we could find (even if 0 fit, meaning natural game over).
        return bestBatch.ifEmpty { BlockShapeFactory.spawnBlocks(count = 3, difficulty = difficulty).map { it as Block? } }
    }

    private fun canPlaceAnywhere(board: List<List<BoardCell>>, block: Block): Boolean {
        for (r in board.indices) {
            for (c in board[0].indices) {
                if (placeBlockUseCase.canPlace(board, block, r, c)) {
                    return true
                }
            }
        }
        return false
    }
}

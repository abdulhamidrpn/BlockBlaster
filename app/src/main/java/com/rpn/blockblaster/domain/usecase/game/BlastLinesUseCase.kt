package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.BlastResult
import com.rpn.blockblaster.domain.model.BoardCell
import com.rpn.blockblaster.domain.model.BOARD_SIZE

class BlastLinesUseCase {

    fun detect(board: List<List<BoardCell>>): BlastResult {
        val completedRows = (0 until BOARD_SIZE).filter { r ->
            (0 until BOARD_SIZE).all { c -> board[r][c].isFilled }
        }
        val completedCols = (0 until BOARD_SIZE).filter { c ->
            (0 until BOARD_SIZE).all { r -> board[r][c].isFilled }
        }
        if (completedRows.isEmpty() && completedCols.isEmpty()) return BlastResult()

        val blastingCells = mutableSetOf<Pair<Int,Int>>()
        completedRows.forEach { r -> (0 until BOARD_SIZE).forEach { c -> blastingCells.add(Pair(r,c)) } }
        completedCols.forEach { c -> (0 until BOARD_SIZE).forEach { r -> blastingCells.add(Pair(r,c)) } }

        val isCross     = completedRows.isNotEmpty() && completedCols.isNotEmpty()
        val lineCount   = completedRows.size + completedCols.size
        val basePoints  = lineCount * 80
        val crossBonus  = if (isCross) 200 else 0
        val multiMult   = when {
            lineCount >= 4 -> 2.5f
            lineCount == 3 -> 2.0f
            lineCount == 2 -> 1.5f
            else           -> 1.0f
        }
        val points = (basePoints * multiMult).toInt() + crossBonus

        // Check perfect clear on the cleared board
        val clearedBoard  = clear(board, blastingCells)
        val isPerfect     = clearedBoard.all { row -> row.all { !it.isFilled } }
        val finalPoints   = points + if (isPerfect) 500 else 0

        return BlastResult(
            rows           = completedRows,
            cols           = completedCols,
            pointsAwarded  = finalPoints,
            isCrossBlast   = isCross,
            isPerfectClear = isPerfect,
            blastingCells  = blastingCells
        )
    }

    fun clear(board: List<List<BoardCell>>, cells: Set<Pair<Int,Int>>): List<List<BoardCell>> {
        val nb = board.map { it.toMutableList() }.toMutableList()
        cells.forEach { (r, c) -> nb[r][c] = BoardCell() }
        return nb
    }
}

package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.Block
import com.rpn.blockblaster.domain.model.BoardCell
import com.rpn.blockblaster.domain.model.BOARD_SIZE

class PlaceBlockUseCase {

    fun canPlace(board: List<List<BoardCell>>, block: Block, row: Int, col: Int): Boolean {
        if (row < 0 || col < 0) return false
        block.shape.forEachIndexed { dr, rowShape ->
            rowShape.forEachIndexed { dc, filled ->
                if (filled) {
                    val r = row + dr; val c = col + dc
                    if (r >= BOARD_SIZE || c >= BOARD_SIZE || r < 0 || c < 0) return false
                    if (board[r][c].isFilled) return false
                }
            }
        }
        return true
    }

    fun place(board: List<List<BoardCell>>, block: Block, row: Int, col: Int): List<List<BoardCell>> {
        val newBoard = board.map { it.toMutableList() }.toMutableList()
        block.shape.forEachIndexed { dr, rowShape ->
            rowShape.forEachIndexed { dc, filled ->
                if (filled) {
                    val r = row + dr; val c = col + dc
                    newBoard[r][c] = newBoard[r][c].copy(isFilled = true, color = block.color)
                }
            }
        }
        return newBoard
    }

    fun getPlacementCells(block: Block, row: Int, col: Int): Set<Pair<Int,Int>> {
        val cells = mutableSetOf<Pair<Int,Int>>()
        block.shape.forEachIndexed { dr, rowShape ->
            rowShape.forEachIndexed { dc, filled ->
                if (filled) cells.add(Pair(row + dr, col + dc))
            }
        }
        return cells
    }
}

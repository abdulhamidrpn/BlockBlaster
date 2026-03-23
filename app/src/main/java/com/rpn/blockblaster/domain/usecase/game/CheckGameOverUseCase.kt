package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.Block
import com.rpn.blockblaster.domain.model.BoardCell
import com.rpn.blockblaster.domain.model.BOARD_SIZE

class CheckGameOverUseCase(private val placeUseCase: PlaceBlockUseCase) {
    operator fun invoke(board: List<List<BoardCell>>, tray: List<Block?>): Boolean {
        val active = tray.filterNotNull()
        if (active.isEmpty()) return false
        return active.all { block ->
            (0 until BOARD_SIZE).all { row ->
                (0 until BOARD_SIZE).all { col ->
                    !placeUseCase.canPlace(board, block, row, col)
                }
            }
        }
    }
}

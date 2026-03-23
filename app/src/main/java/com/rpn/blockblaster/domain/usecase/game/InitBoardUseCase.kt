package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.BoardCell
import com.rpn.blockblaster.domain.model.BOARD_SIZE

class InitBoardUseCase {
    operator fun invoke(): List<List<BoardCell>> =
        List(BOARD_SIZE) { List(BOARD_SIZE) { BoardCell() } }
}

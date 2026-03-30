package com.rpn.blockblaster.domain.model

import androidx.compose.ui.graphics.Color

const val BOARD_SIZE = 8

fun emptyBoard(): List<List<BoardCell>> =
    List(BOARD_SIZE) { List(BOARD_SIZE) { BoardCell() } }

data class DragState(
    val blockIndex: Int,
    val block:      Block
)

data class BlastResult(
    val rows:          List<Int> = emptyList(),
    val cols:          List<Int> = emptyList(),
    val pointsAwarded: Int       = 0,
    val isCrossBlast:  Boolean   = false,
    val isPerfectClear:Boolean   = false,
    val blastingCells: Set<Pair<Int,Int>> = emptySet()
)

data class ScorePopup(
    val id:          Long   = System.currentTimeMillis(),
    val comboText:   String? = null,
    val pointsText:  String  = "",
    val messageText: String? = null,
    val comboColor:  Color   = Color.White,
    val pointsColor: Color   = Color.White,
    val messageColor:Color   = Color.White,
    val row:         Int    = 3,
    val col:         Int    = 3
)

data class Milestone(val id: String, val message: String)

sealed class GamePhase {
    object Playing      : GamePhase()
    object Blasting     : GamePhase()
    object RevivePrompt : GamePhase()
    object GameOver     : GamePhase()
    object Paused       : GamePhase()
}

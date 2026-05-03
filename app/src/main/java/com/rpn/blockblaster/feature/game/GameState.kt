package com.rpn.blockblaster.feature.game

import com.rpn.blockblaster.domain.engine.Difficulty
import com.rpn.blockblaster.domain.model.*

data class GameState(
    val board:             List<List<BoardCell>> = emptyBoard(),
    val trayBlocks:        List<Block?>          = listOf(null, null, null),
    val difficulty:        Difficulty            = Difficulty.MEDIUM,
    val currentScore:      Int                   = 0,
    val bestScore:         Int                   = 0,
    val displayScore:      Int                   = 0,
    val comboStreak:       Int                   = 0,
    val blastingCells:     Set<Pair<Int,Int>>    = emptySet(),
    val highlightCells:    Set<Pair<Int,Int>>    = emptySet(),
    val isHighlightValid:  Boolean               = true,
    // Active drag – only tray index + block; screen position stays local in UI
    val dragState:         DragState?            = null,
    val phase:             GamePhase             = GamePhase.Playing,
    val canRevive:         Boolean               = true,
    val reviveCountdown:   Int                   = 5,
    val isNewBestScore:    Boolean               = false,
    val popupMessages:     List<ScorePopup>      = emptyList(),
    val sessionStats:      SessionStats          = SessionStats(),
    val settings:          AppSettings           = AppSettings(),
    // Board layout – measured once from the composable
    val boardOriginX:      Float                 = 0f,
    val boardOriginY:      Float                 = 0f,
    val cellSize:          Float                 = 0f
)

data class SessionStats(
    val linesBlasted: Int   = 0,
    val crossBlasts:  Int   = 0,
    val bestCombo:    Float = 1f,
    val blocksPlaced: Int   = 0
)

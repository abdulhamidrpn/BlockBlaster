package com.rpn.blockblaster.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BoardCellState(
    val isFilled: Boolean,
    val colorArgb: Long?
)

@Serializable
data class BlockState(
    val shape: List<List<Boolean>>,
    val colorArgb: Long,
    val name: String
)

@Serializable
data class GamePersistenceState(
    val board: List<List<BoardCellState>>,
    val tray: List<BlockState?>,
    val score: Int,
    val comboStreak: Int,
    val difficulty: String,
    val canRevive: Boolean,
    val timestamp: Long
)

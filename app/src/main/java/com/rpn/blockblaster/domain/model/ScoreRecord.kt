package com.rpn.blockblaster.domain.model

data class ScoreRecord(
    val id:           Int  = 0,
    val score:        Int,
    val timestamp:    Long,
    val difficulty:   String = "MEDIUM",
    val linesBlasted: Int  = 0,
    val crossBlasts:  Int  = 0,
    val bestCombo:    Float = 1f,
    val blocksPlaced: Int  = 0
)

package com.rpn.blockblaster.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id:   Int  = 0,
    val score:        Int,
    val timestamp:    Long,
    val difficulty:   String = "MEDIUM",
    val linesBlasted: Int   = 0,
    val crossBlasts:  Int   = 0,
    val bestCombo:    Float = 1f,
    val blocksPlaced: Int   = 0
)

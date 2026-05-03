package com.rpn.blockblaster.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey val id: Int = 1,
    val boardJson: String,
    val trayJson: String,
    val score: Int,
    val comboStreak: Int,
    val difficulty: String,
    val canRevive: Boolean,
    val timestamp: Long
)

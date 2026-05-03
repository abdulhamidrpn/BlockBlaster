package com.rpn.blockblaster.domain.model

import com.rpn.blockblaster.domain.engine.Difficulty

data class AppSettings(
    val soundEnabled:    Boolean  = true,
    val bgmEnabled:      Boolean  = true,
    val vibrationEnabled:Boolean  = true,
    val isDarkTheme:     Boolean  = true,
    val showGridLines:   Boolean  = true,
    val animSpeedMs:     Int      = 300,  // normal speed
    val gamesPlayed:     Int      = 0,
    val difficulty:      Difficulty = Difficulty.MEDIUM
)

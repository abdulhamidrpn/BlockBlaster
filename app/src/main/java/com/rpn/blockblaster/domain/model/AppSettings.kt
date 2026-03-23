package com.rpn.blockblaster.domain.model

data class AppSettings(
    val soundEnabled:    Boolean  = true,
    val bgmEnabled:      Boolean  = true,
    val vibrationEnabled:Boolean  = true,
    val isDarkTheme:     Boolean  = true,
    val showGridLines:   Boolean  = true,
    val animSpeedMs:     Int      = 300   // normal speed
)

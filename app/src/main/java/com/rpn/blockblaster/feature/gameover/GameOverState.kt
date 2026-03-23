package com.rpn.blockblaster.feature.gameover
data class GameOverState(
    val finalScore:  Int     = 0,
    val bestScore:   Int     = 0,
    val isNewBest:   Boolean = false
)

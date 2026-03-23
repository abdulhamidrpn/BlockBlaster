package com.rpn.blockblaster.feature.gameover
sealed class GameOverIntent {
    object PlayAgain : GameOverIntent()
    object GoHome    : GameOverIntent()
    object Share     : GameOverIntent()
}

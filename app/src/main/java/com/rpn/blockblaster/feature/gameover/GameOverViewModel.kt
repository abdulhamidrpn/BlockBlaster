package com.rpn.blockblaster.feature.gameover

import com.rpn.blockblaster.core.common.MviViewModel

sealed class GameOverUiEvent {
    object NavigateGame : GameOverUiEvent()
    object NavigateHome : GameOverUiEvent()
    data class Share(val score: Int) : GameOverUiEvent()
}

class GameOverViewModel : MviViewModel<GameOverState, GameOverIntent, GameOverUiEvent>(GameOverState()) {
    override fun onIntent(intent: GameOverIntent) {
        when (intent) {
            is GameOverIntent.PlayAgain -> sendEvent(GameOverUiEvent.NavigateGame)
            is GameOverIntent.GoHome    -> sendEvent(GameOverUiEvent.NavigateHome)
            is GameOverIntent.Share     -> sendEvent(GameOverUiEvent.Share(currentState.finalScore))
        }
    }
    fun init(finalScore: Int, bestScore: Int) {
        setState { copy(finalScore = finalScore, bestScore = bestScore, isNewBest = finalScore >= bestScore && finalScore > 0) }
    }
}

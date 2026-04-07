package com.rpn.blockblaster.feature.gameover

import com.rpn.blockblaster.core.common.MviViewModel

import androidx.lifecycle.viewModelScope
import com.rpn.blockblaster.domain.usecase.settings.GetSettingsUseCase
import com.rpn.blockblaster.domain.usecase.settings.SaveSettingsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class GameOverUiEvent {
    object NavigateGame : GameOverUiEvent()
    object NavigateHome : GameOverUiEvent()
    data class Share(val score: Int) : GameOverUiEvent()
    object TriggerReview : GameOverUiEvent()
}

class GameOverViewModel(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase
) : MviViewModel<GameOverState, GameOverIntent, GameOverUiEvent>(GameOverState()) {
    override fun onIntent(intent: GameOverIntent) {
        when (intent) {
            is GameOverIntent.PlayAgain -> sendEvent(GameOverUiEvent.NavigateGame)
            is GameOverIntent.GoHome    -> sendEvent(GameOverUiEvent.NavigateHome)
            is GameOverIntent.Share     -> sendEvent(GameOverUiEvent.Share(currentState.finalScore))
        }
    }
    fun init(finalScore: Int, bestScore: Int) {
        setState { copy(finalScore = finalScore, bestScore = bestScore, isNewBest = finalScore >= bestScore && finalScore > 0) }
        viewModelScope.launch {
            val settings = getSettings().first()
            val newPlayed = settings.gamesPlayed + 1
            saveSettings(settings.copy(gamesPlayed = newPlayed))
            if (newPlayed == 1) {
                sendEvent(GameOverUiEvent.TriggerReview)
            }
        }
    }
}

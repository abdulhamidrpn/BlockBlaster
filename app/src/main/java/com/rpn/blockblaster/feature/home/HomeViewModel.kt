package com.rpn.blockblaster.feature.home

import androidx.lifecycle.viewModelScope
import com.rpn.blockblaster.core.common.MviViewModel
import com.rpn.blockblaster.domain.engine.Difficulty
import com.rpn.blockblaster.domain.usecase.score.GetBestScoreUseCase
import com.rpn.blockblaster.domain.usecase.settings.GetSettingsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class HomeUiEvent {
    object NavigateGame     : HomeUiEvent()
    object NavigateSettings : HomeUiEvent()
}

class HomeViewModel(
    private val getBestScore: GetBestScoreUseCase,
    private val getSettings:  GetSettingsUseCase
) : MviViewModel<HomeState, HomeIntent, HomeUiEvent>(HomeState()) {

    init { onIntent(HomeIntent.LoadBestScore) }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadBestScore -> viewModelScope.launch {
                val easy   = getBestScore("EASY")
                val medium = getBestScore("MEDIUM")
                val hard   = getBestScore("HARD")
                setState { 
                    copy(
                        bestScoreEasy   = easy, 
                        bestScoreMedium = medium, 
                        bestScoreHard   = hard, 
                        isLoading       = false
                    ) 
                }
            }
            is HomeIntent.NavigateToGame     -> sendEvent(HomeUiEvent.NavigateGame)
            is HomeIntent.NavigateToSettings -> sendEvent(HomeUiEvent.NavigateSettings)
        }
    }
}

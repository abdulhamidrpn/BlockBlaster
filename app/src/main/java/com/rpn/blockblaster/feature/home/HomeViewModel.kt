package com.rpn.blockblaster.feature.home

import androidx.lifecycle.viewModelScope
import com.rpn.blockblaster.core.common.MviViewModel
import com.rpn.blockblaster.domain.usecase.score.GetBestScoreUseCase
import kotlinx.coroutines.launch

sealed class HomeUiEvent {
    object NavigateGame     : HomeUiEvent()
    object NavigateSettings : HomeUiEvent()
}

class HomeViewModel(
    private val getBestScore: GetBestScoreUseCase
) : MviViewModel<HomeState, HomeIntent, HomeUiEvent>(HomeState()) {

    init { onIntent(HomeIntent.LoadBestScore) }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadBestScore -> viewModelScope.launch {
                val best = getBestScore()
                setState { copy(bestScore = best, isLoading = false) }
            }
            is HomeIntent.NavigateToGame     -> sendEvent(HomeUiEvent.NavigateGame)
            is HomeIntent.NavigateToSettings -> sendEvent(HomeUiEvent.NavigateSettings)
        }
    }
}

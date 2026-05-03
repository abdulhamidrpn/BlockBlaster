package com.rpn.blockblaster.feature.home

import com.rpn.blockblaster.domain.engine.Difficulty

sealed class HomeIntent {
    object LoadBestScore : HomeIntent()
    object NavigateToGame : HomeIntent()
    object NavigateToSettings : HomeIntent()
}

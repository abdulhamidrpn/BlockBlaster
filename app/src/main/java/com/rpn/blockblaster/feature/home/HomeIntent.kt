package com.rpn.blockblaster.feature.home

sealed class HomeIntent {
    object LoadBestScore : HomeIntent()
    object NavigateToGame : HomeIntent()
    object NavigateToSettings : HomeIntent()
}

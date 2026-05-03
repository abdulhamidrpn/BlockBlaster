package com.rpn.blockblaster.feature.home

data class HomeState(
    val bestScoreEasy:   Int = 0,
    val bestScoreMedium: Int = 0,
    val bestScoreHard:   Int = 0,
    val isLoading:       Boolean = true
) {
    val bestScore: Int get() = maxOf(bestScoreEasy, bestScoreMedium, bestScoreHard)
}

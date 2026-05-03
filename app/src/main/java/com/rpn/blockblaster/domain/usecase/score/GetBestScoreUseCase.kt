package com.rpn.blockblaster.domain.usecase.score

import com.rpn.blockblaster.domain.repository.ScoreRepository

class GetBestScoreUseCase(private val repo: ScoreRepository) {
    suspend operator fun invoke(difficulty: String = "MEDIUM"): Int = repo.getBestScore(difficulty)
}

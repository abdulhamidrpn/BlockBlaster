package com.rpn.blockblaster.domain.usecase.score

import com.rpn.blockblaster.domain.model.ScoreRecord
import com.rpn.blockblaster.domain.repository.ScoreRepository

class SaveScoreUseCase(private val repo: ScoreRepository) {
    suspend operator fun invoke(record: ScoreRecord) = repo.insert(record)
}

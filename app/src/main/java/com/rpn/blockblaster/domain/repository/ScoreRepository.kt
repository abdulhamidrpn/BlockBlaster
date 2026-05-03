package com.rpn.blockblaster.domain.repository

import com.rpn.blockblaster.domain.model.ScoreRecord

interface ScoreRepository {
    suspend fun insert(record: ScoreRecord)
    suspend fun getBestScore(difficulty: String): Int
    suspend fun getTopScores(difficulty: String): List<ScoreRecord>
    suspend fun clearAll()
}

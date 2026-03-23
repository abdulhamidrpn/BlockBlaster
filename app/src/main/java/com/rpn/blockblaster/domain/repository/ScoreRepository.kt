package com.rpn.blockblaster.domain.repository

import com.rpn.blockblaster.domain.model.ScoreRecord

interface ScoreRepository {
    suspend fun insert(record: ScoreRecord)
    suspend fun getBestScore(): Int
    suspend fun getTopScores(): List<ScoreRecord>
    suspend fun clearAll()
}

package com.rpn.blockblaster.data.repository

import com.rpn.blockblaster.data.local.db.ScoreDao
import com.rpn.blockblaster.data.local.db.entity.ScoreEntity
import com.rpn.blockblaster.domain.model.ScoreRecord
import com.rpn.blockblaster.domain.repository.ScoreRepository

class ScoreRepositoryImpl(private val dao: ScoreDao) : ScoreRepository {

    override suspend fun insert(record: ScoreRecord) {
        dao.insert(ScoreEntity(
            score        = record.score,
            timestamp    = record.timestamp,
            difficulty   = record.difficulty,
            linesBlasted = record.linesBlasted,
            crossBlasts  = record.crossBlasts,
            bestCombo    = record.bestCombo,
            blocksPlaced = record.blocksPlaced
        ))
    }

    override suspend fun getBestScore(difficulty: String): Int = dao.getBestScore(difficulty) ?: 0

    override suspend fun getTopScores(difficulty: String): List<ScoreRecord> =
        dao.getTopScores(difficulty).map {
            ScoreRecord(
                id           = it.id,
                score        = it.score,
                timestamp    = it.timestamp,
                difficulty   = it.difficulty,
                linesBlasted = it.linesBlasted,
                crossBlasts  = it.crossBlasts,
                bestCombo    = it.bestCombo,
                blocksPlaced = it.blocksPlaced
            )
        }

    override suspend fun clearAll() = dao.clearAll()
}

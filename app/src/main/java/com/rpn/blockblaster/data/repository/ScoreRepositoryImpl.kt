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
            linesBlasted = record.linesBlasted,
            crossBlasts  = record.crossBlasts,
            bestCombo    = record.bestCombo,
            blocksPlaced = record.blocksPlaced
        ))
    }

    override suspend fun getBestScore(): Int = dao.getBestScore() ?: 0

    override suspend fun getTopScores(): List<ScoreRecord> =
        dao.getTopScores().map {
            ScoreRecord(
                id           = it.id,
                score        = it.score,
                timestamp    = it.timestamp,
                linesBlasted = it.linesBlasted,
                crossBlasts  = it.crossBlasts,
                bestCombo    = it.bestCombo,
                blocksPlaced = it.blocksPlaced
            )
        }

    override suspend fun clearAll() = dao.clearAll()
}

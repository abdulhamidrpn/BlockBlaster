package com.rpn.blockblaster.data.local.db

import androidx.room.*
import com.rpn.blockblaster.data.local.db.entity.ScoreEntity

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT MAX(score) FROM scores WHERE difficulty = :difficulty")
    suspend fun getBestScore(difficulty: String): Int?

    @Query("SELECT * FROM scores WHERE difficulty = :difficulty ORDER BY score DESC LIMIT 10")
    suspend fun getTopScores(difficulty: String): List<ScoreEntity>

    @Query("DELETE FROM scores")
    suspend fun clearAll()
}

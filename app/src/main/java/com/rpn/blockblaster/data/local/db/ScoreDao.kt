package com.rpn.blockblaster.data.local.db

import androidx.room.*
import com.rpn.blockblaster.data.local.db.entity.ScoreEntity

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: ScoreEntity)

    @Query("SELECT MAX(score) FROM scores")
    suspend fun getBestScore(): Int?

    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 10")
    suspend fun getTopScores(): List<ScoreEntity>

    @Query("DELETE FROM scores")
    suspend fun clearAll()
}

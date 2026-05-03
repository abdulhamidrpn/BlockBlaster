package com.rpn.blockblaster.data.local.db

import androidx.room.*
import com.rpn.blockblaster.data.local.db.entity.GameStateEntity

@Dao
interface GamePersistenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(state: GameStateEntity)

    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getSavedGame(): GameStateEntity?

    @Query("DELETE FROM game_state")
    suspend fun clearSavedGame()
}

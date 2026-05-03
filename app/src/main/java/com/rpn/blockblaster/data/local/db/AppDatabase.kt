package com.rpn.blockblaster.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rpn.blockblaster.data.local.db.entity.GameStateEntity
import com.rpn.blockblaster.data.local.db.entity.ScoreEntity

@Database(entities = [ScoreEntity::class, GameStateEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
    abstract fun gamePersistenceDao(): GamePersistenceDao
}

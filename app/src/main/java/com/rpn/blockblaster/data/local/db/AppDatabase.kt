package com.rpn.blockblaster.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rpn.blockblaster.data.local.db.entity.ScoreEntity

@Database(entities = [ScoreEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao
}

package com.rpn.blockblaster.core.di

import androidx.room.Room
import com.rpn.blockblaster.data.local.db.AppDatabase
import com.rpn.blockblaster.data.repository.GamePersistenceRepositoryImpl
import com.rpn.blockblaster.data.repository.ScoreRepositoryImpl
import com.rpn.blockblaster.domain.repository.GamePersistenceRepository
import com.rpn.blockblaster.domain.repository.ScoreRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "blockblaster.db")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<AppDatabase>().scoreDao() }
    single { get<AppDatabase>().gamePersistenceDao() }
    
    single<ScoreRepository> { ScoreRepositoryImpl(get()) }
    single<GamePersistenceRepository> { GamePersistenceRepositoryImpl(get()) }
}

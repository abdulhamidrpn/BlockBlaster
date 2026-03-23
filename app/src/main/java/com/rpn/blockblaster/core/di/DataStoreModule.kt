package com.rpn.blockblaster.core.di

import com.rpn.blockblaster.data.local.datastore.SettingsDataStore
import com.rpn.blockblaster.data.repository.SettingsRepositoryImpl
import com.rpn.blockblaster.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataStoreModule = module {
    single { SettingsDataStore(androidContext()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

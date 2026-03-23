package com.rpn.blockblaster.data.repository

import com.rpn.blockblaster.data.local.datastore.SettingsDataStore
import com.rpn.blockblaster.domain.model.AppSettings
import com.rpn.blockblaster.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class SettingsRepositoryImpl(private val ds: SettingsDataStore) : SettingsRepository {
    override fun getSettings(): Flow<AppSettings> = ds.getSettings()
    override suspend fun saveSettings(settings: AppSettings) = ds.save(settings)
}

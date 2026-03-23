package com.rpn.blockblaster.domain.usecase.settings

import com.rpn.blockblaster.domain.model.AppSettings
import com.rpn.blockblaster.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class GetSettingsUseCase(private val repo: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = repo.getSettings()
}

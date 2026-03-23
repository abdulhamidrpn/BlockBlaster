package com.rpn.blockblaster.domain.usecase.settings

import com.rpn.blockblaster.domain.model.AppSettings
import com.rpn.blockblaster.domain.repository.SettingsRepository

class SaveSettingsUseCase(private val repo: SettingsRepository) {
    suspend operator fun invoke(settings: AppSettings) = repo.saveSettings(settings)
}

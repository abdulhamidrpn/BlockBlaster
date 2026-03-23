package com.rpn.blockblaster.feature.settings

import com.rpn.blockblaster.domain.model.AppSettings

sealed class SettingsIntent {
    object Load                                 : SettingsIntent()
    data class Save(val settings: AppSettings)  : SettingsIntent()
    object ClearBestScore                       : SettingsIntent()
    object NavigateBack                         : SettingsIntent()
}

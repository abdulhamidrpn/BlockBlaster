package com.rpn.blockblaster.feature.settings

import com.rpn.blockblaster.domain.model.AppSettings

data class SettingsState(
    val settings:  AppSettings = AppSettings(),
    val isLoading: Boolean     = true
)

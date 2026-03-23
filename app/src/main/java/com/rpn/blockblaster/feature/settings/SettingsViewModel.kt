package com.rpn.blockblaster.feature.settings

import androidx.lifecycle.viewModelScope
import com.rpn.blockblaster.core.common.MviViewModel
import com.rpn.blockblaster.domain.usecase.settings.GetSettingsUseCase
import com.rpn.blockblaster.domain.usecase.settings.SaveSettingsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class SettingsUiEvent {
    object NavigateBack : SettingsUiEvent()
}

class SettingsViewModel(
    private val getSettings:  GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase
) : MviViewModel<SettingsState, SettingsIntent, SettingsUiEvent>(SettingsState()) {

    init { onIntent(SettingsIntent.Load) }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.Load -> viewModelScope.launch {
                getSettings().collectLatest { settings ->
                    setState { copy(settings = settings, isLoading = false) }
                }
            }
            is SettingsIntent.Save -> viewModelScope.launch {
                saveSettings(intent.settings)
            }
            is SettingsIntent.ClearBestScore -> { /* handled via repo */ }
            is SettingsIntent.NavigateBack   -> sendEvent(SettingsUiEvent.NavigateBack)
        }
    }
}

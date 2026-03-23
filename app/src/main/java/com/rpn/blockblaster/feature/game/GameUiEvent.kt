package com.rpn.blockblaster.feature.game

import com.rpn.blockblaster.service.SoundType

sealed class GameUiEvent {
    data class PlaySound(val type: SoundType, val pitch: Float = 1f) : GameUiEvent()
    object VibrateLight                                               : GameUiEvent()
    object VibrateHeavy                                               : GameUiEvent()
    data class NavigateGameOver(val score: Int, val best: Int)        : GameUiEvent()
    object NavigateHome                                               : GameUiEvent()
    object ReplayGame                                                 : GameUiEvent()
    data class ShowBanner(val message: String)                        : GameUiEvent()
    // Sent when a drop lands on an invalid position → triggers shake + snap-back in UI
    data class InvalidDrop(val slotIndex: Int)                        : GameUiEvent()
}

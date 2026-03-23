package com.rpn.blockblaster.feature.game

sealed class GameIntent {
    object StartGame                                                   : GameIntent()
    object PauseGame                                                   : GameIntent()
    object ResumeGame                                                  : GameIntent()
    data class StartDrag(val index: Int)                               : GameIntent()
    // VM receives the grid cell directly – no coordinate math in the VM
    data class UpdateDragCell(val row: Int, val col: Int)              : GameIntent()
    data class DropBlock(val row: Int, val col: Int)                   : GameIntent()
    data class DropBlockInTray(val fromIdx: Int, val toIdx: Int)       : GameIntent()
    object CancelDrag                                                  : GameIntent()
    object AcceptRevive                                                : GameIntent()
    object DeclineRevive                                               : GameIntent()
    object PauseReviveTimer                                            : GameIntent()
    object NavigateHome                                                : GameIntent()
    object ReplayGame                                                  : GameIntent()
    data class SetBoardLayout(val x: Float, val y: Float,
                              val width: Float)                        : GameIntent()
    data class SetTrayLayout(val y: Float, val height: Float)          : GameIntent()
    object BlastAnimationDone                                          : GameIntent()
    object DismissPopup                                                : GameIntent()
}

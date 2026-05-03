package com.rpn.blockblaster.domain.repository

import com.rpn.blockblaster.domain.model.GamePersistenceState

interface GamePersistenceRepository {
    suspend fun saveGame(state: GamePersistenceState)
    suspend fun loadGame(): GamePersistenceState?
    suspend fun clearGame()
}

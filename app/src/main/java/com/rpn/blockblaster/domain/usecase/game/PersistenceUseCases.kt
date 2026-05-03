package com.rpn.blockblaster.domain.usecase.game

import com.rpn.blockblaster.domain.model.GamePersistenceState
import com.rpn.blockblaster.domain.repository.GamePersistenceRepository

class SaveGameStateUseCase(private val repo: GamePersistenceRepository) {
    suspend operator fun invoke(state: GamePersistenceState) = repo.saveGame(state)
}

class LoadGameStateUseCase(private val repo: GamePersistenceRepository) {
    suspend operator fun invoke(): GamePersistenceState? = repo.loadGame()
}

class ClearGameStateUseCase(private val repo: GamePersistenceRepository) {
    suspend operator fun invoke() = repo.clearGame()
}

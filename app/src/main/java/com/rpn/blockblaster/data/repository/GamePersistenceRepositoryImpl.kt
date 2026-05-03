package com.rpn.blockblaster.data.repository

import com.rpn.blockblaster.data.local.db.GamePersistenceDao
import com.rpn.blockblaster.data.local.db.entity.GameStateEntity
import com.rpn.blockblaster.domain.model.GamePersistenceState
import com.rpn.blockblaster.domain.repository.GamePersistenceRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GamePersistenceRepositoryImpl(
    private val dao: GamePersistenceDao
) : GamePersistenceRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveGame(state: GamePersistenceState) {
        val entity = GameStateEntity(
            boardJson = json.encodeToString(state.board),
            trayJson = json.encodeToString(state.tray),
            score = state.score,
            comboStreak = state.comboStreak,
            difficulty = state.difficulty,
            canRevive = state.canRevive,
            timestamp = state.timestamp
        )
        dao.saveGame(entity)
    }

    override suspend fun loadGame(): GamePersistenceState? {
        val entity = dao.getSavedGame() ?: return null
        return GamePersistenceState(
            board = json.decodeFromString(entity.boardJson),
            tray = json.decodeFromString(entity.trayJson),
            score = entity.score,
            comboStreak = entity.comboStreak,
            difficulty = entity.difficulty,
            canRevive = entity.canRevive,
            timestamp = entity.timestamp
        )
    }

    override suspend fun clearGame() {
        dao.clearSavedGame()
    }
}

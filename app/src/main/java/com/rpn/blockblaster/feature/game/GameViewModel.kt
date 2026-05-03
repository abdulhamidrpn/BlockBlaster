package com.rpn.blockblaster.feature.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.rpn.blockblaster.core.common.MviViewModel
import com.rpn.blockblaster.core.designsystem.AccentRed
import com.rpn.blockblaster.core.designsystem.BlockMint
import com.rpn.blockblaster.core.designsystem.BlockTeal
import com.rpn.blockblaster.core.designsystem.GoldColor
import com.rpn.blockblaster.domain.engine.Difficulty
import com.rpn.blockblaster.domain.model.*
import com.rpn.blockblaster.domain.usecase.game.*
import com.rpn.blockblaster.domain.usecase.score.*
import com.rpn.blockblaster.domain.usecase.settings.GetSettingsUseCase
import com.rpn.blockblaster.service.SoundManager
import com.rpn.blockblaster.service.SoundType
import com.rpn.blockblaster.service.VibrationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

class GameViewModel(
    private val initBoard:        InitBoardUseCase,
    private val placeBlock:       PlaceBlockUseCase,
    private val blastLines:       BlastLinesUseCase,
    private val spawnBlocks:      SpawnBlocksUseCase,
    private val checkGameOver:    CheckGameOverUseCase,
    private val calcScore:        CalculateScoreUseCase,
    private val saveScore:        SaveScoreUseCase,
    private val getBestScore:     GetBestScoreUseCase,
    private val getSettings:      GetSettingsUseCase,
    private val saveGameState:    SaveGameStateUseCase,
    private val loadGameState:    LoadGameStateUseCase,
    private val clearGameState:   ClearGameStateUseCase,
    private val soundManager:     SoundManager,
    private val vibrationManager: VibrationManager
) : MviViewModel<GameState, GameIntent, GameUiEvent>(GameState()) {

    private var reviveJob:    Job? = null
    private var scoreAnimJob: Job? = null
    private var settingsJob:  Job? = null

    init { /* Handled by GameScreen via Started intent */ }

    override fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartGame      -> startGame()
            is GameIntent.PauseGame      -> { soundManager.play(SoundType.BUTTON_CLICK); setState { copy(phase = GamePhase.Paused) } }
            is GameIntent.ResumeGame     -> { soundManager.play(SoundType.BUTTON_CLICK); setState { copy(phase = GamePhase.Playing) } }
            is GameIntent.StartDrag      -> handleStartDrag(intent.index)
            is GameIntent.UpdateDragCell -> handleUpdateDragCell(intent.row, intent.col)
            is GameIntent.DropBlock      -> handleDrop(intent.row, intent.col)
            is GameIntent.DropBlockInTray-> handleDropInTray(intent.fromIdx, intent.toIdx)
            is GameIntent.CancelDrag     -> clearDrag()
            is GameIntent.AcceptRevive   -> handleRevive()
            is GameIntent.DeclineRevive  -> { soundManager.play(SoundType.BUTTON_CLICK); endGame() }
            is GameIntent.PauseReviveTimer -> reviveJob?.cancel()
            is GameIntent.NavigateHome   -> { soundManager.play(SoundType.BUTTON_CLICK); saveCurrentState(); sendEvent(GameUiEvent.NavigateHome) }
            is GameIntent.ReplayGame     -> { soundManager.play(SoundType.BUTTON_CLICK); viewModelScope.launch { clearGameState() }; startGame() }
            is GameIntent.SetBoardLayout -> setState {
                copy(boardOriginX = intent.x, boardOriginY = intent.y,
                    cellSize = intent.width / BOARD_SIZE.toFloat())
            }
            is GameIntent.SetTrayLayout  -> {} // Just cache for drop detection
            is GameIntent.BlastAnimationDone -> setState { copy(blastingCells = emptySet()) }
            is GameIntent.DismissPopup   -> setState { copy(popupMessages = emptyList()) }
        }
    }

    private fun startGame() {
        reviveJob?.cancel(); scoreAnimJob?.cancel(); settingsJob?.cancel()
        viewModelScope.launch {
            val settings = getSettings().first()
            val currentSettingsDifficulty = settings.difficulty
            
            val saved = loadGameState()
            // Only restore if saved game difficulty matches current settings
            if (saved != null && saved.difficulty == currentSettingsDifficulty.name) {
                // Restore saved game
                val board = saved.board.map { row ->
                    row.map { cell ->
                        BoardCell(
                            isFilled = cell.isFilled,
                            color = cell.colorArgb?.let { Color(it.toULong()) }
                        )
                    }
                }
                val tray = saved.tray.map { bState ->
                    bState?.let {
                        Block(
                            shape = it.shape,
                            color = Color(it.colorArgb.toULong()),
                            name = it.name
                        )
                    }
                }
                val best = getBestScore(currentSettingsDifficulty.name)
                setState {
                    GameState(
                        board = board,
                        trayBlocks = tray,
                        currentScore = saved.score,
                        displayScore = saved.score,
                        bestScore = best,
                        difficulty = currentSettingsDifficulty,
                        comboStreak = saved.comboStreak,
                        canRevive = saved.canRevive
                    )
                }
            } else {
                // Start fresh with settings difficulty
                val best  = getBestScore(currentSettingsDifficulty.name)
                val board = initBoard()
                val tray  = spawnBlocks(board, currentSettingsDifficulty)
                setState {
                    GameState(
                        board = board, trayBlocks = tray, bestScore = best,
                        difficulty = currentSettingsDifficulty,
                        canRevive = true, displayScore = 0
                    )
                }
                saveCurrentState()
            }
        }
        settingsJob = viewModelScope.launch {
            getSettings().collect { settings ->
                soundManager.enabled     = settings.soundEnabled
                soundManager.bgmEnabled  = settings.bgmEnabled
                vibrationManager.enabled = settings.vibrationEnabled
                setState { copy(settings = settings) }
            }
        }
    }

    private fun saveCurrentState() {
        val s = currentState
        viewModelScope.launch {
            val persistenceState = GamePersistenceState(
                board = s.board.map { row ->
                    row.map { cell ->
                        BoardCellState(cell.isFilled, cell.color?.value?.toLong())
                    }
                },
                tray = s.trayBlocks.map { block ->
                    block?.let {
                        BlockState(it.shape, it.color.value.toLong(), it.name)
                    }
                },
                score = s.currentScore,
                comboStreak = s.comboStreak,
                difficulty = s.difficulty.name,
                canRevive = s.canRevive,
                timestamp = System.currentTimeMillis()
            )
            saveGameState(persistenceState)
        }
    }

    private fun handleStartDrag(index: Int) {
        val block = currentState.trayBlocks.getOrNull(index) ?: return
        setState { copy(dragState = DragState(index, block), highlightCells = emptySet(), isHighlightValid = true) }
    }

    private fun handleUpdateDragCell(row: Int, col: Int) {
        val ds = currentState.dragState ?: return
        val s  = currentState
        val valid = placeBlock.canPlace(s.board, ds.block, row, col)
        val cells = placeBlock.getPlacementCells(ds.block, row, col)
        if (cells != s.highlightCells || valid != s.isHighlightValid) {
            setState { copy(highlightCells = cells, isHighlightValid = valid) }
        }
    }

    private fun clearDrag() {
        setState { copy(dragState = null, highlightCells = emptySet(), isHighlightValid = true) }
    }

    private fun handleDrop(row: Int, col: Int) {
        val ds = currentState.dragState ?: return
        val s  = currentState

        if (!placeBlock.canPlace(s.board, ds.block, row, col)) {
            soundManager.play(SoundType.INVALID)
            vibrationManager.vibrateLight()
            sendEvent(GameUiEvent.InvalidDrop(ds.blockIndex))
            clearDrag()
            return
        }

        val newBoard = placeBlock.place(s.board, ds.block, row, col)
        val newTray  = s.trayBlocks.toMutableList().also { it[ds.blockIndex] = null }
        val newScore = s.currentScore + calcScore.cellsPlaced(ds.block)
        val newStats = s.sessionStats.copy(blocksPlaced = s.sessionStats.blocksPlaced + 1)

        soundManager.play(SoundType.BLOCK_PLACE)
        vibrationManager.vibrateLight()

        setState {
            copy(
                board = newBoard, trayBlocks = newTray, currentScore = newScore,
                dragState = null, highlightCells = emptySet(), isHighlightValid = true,
                sessionStats = newStats
            )
        }
        animateScore(s.displayScore, newScore)
        saveCurrentState()

        val result = blastLines.detect(newBoard)
        if (result.rows.isNotEmpty() || result.cols.isNotEmpty()) {
            handleBlast(newBoard, newTray, newScore, result, newStats)
        } else {
            setState { copy(comboStreak = 0) }
            if (newTray.all { it == null }) refreshTray(newBoard, currentState.difficulty)
            else checkAndHandleGameOver(newBoard, newTray)
        }
    }

    private fun handleDropInTray(fromIdx: Int, toIdx: Int) {
        val s = currentState
        val fromBlock = s.trayBlocks.getOrNull(fromIdx) ?: return
        val toBlock = s.trayBlocks.getOrNull(toIdx)

        val newTray = s.trayBlocks.toMutableList()
        newTray[fromIdx] = toBlock
        newTray[toIdx] = fromBlock

        sendEvent(GameUiEvent.PlaySound(SoundType.BLOCK_PLACE))
        sendEvent(GameUiEvent.VibrateLight)

        setState {
            copy(
                trayBlocks = newTray,
                dragState = null,
                highlightCells = emptySet(),
                isHighlightValid = true
            )
        }
        saveCurrentState()
    }

    private fun handleBlast(
        board:  List<List<BoardCell>>,
        tray:   List<Block?>,
        score:  Int,
        result: BlastResult,
        stats:  SessionStats
    ) {
        val newStreak = calcScore.newComboStreak(result, currentState.comboStreak)
        val blastPts  = calcScore.blastScore(result, newStreak, currentState.difficulty)
        val newScore  = score + blastPts
        val totalLines = result.rows.size + result.cols.size

        val newStats  = stats.copy(
            linesBlasted = stats.linesBlasted + totalLines,
            crossBlasts  = stats.crossBlasts + if (result.isCrossBlast) 1 else 0,
            bestCombo    = maxOf(stats.bestCombo, calcScore.comboMultiplier(newStreak))
        )

        val comboText = if (newStreak >= 2) "Combo $newStreak" else null
        val comboColor = when {
            newStreak >= 8 -> GoldColor
            newStreak >= 5 -> AccentRed
            newStreak >= 3 -> BlockMint
            else -> BlockTeal
        }

        val pointsText = "+$blastPts"
        val pointsColor = Color.White

        val (msgText, msgColor) = when {
            result.isPerfectClear -> "BOARD CLEAR!!" to GoldColor
            result.isCrossBlast   -> "CROSS BLAST!" to BlockMint
            totalLines == 2       -> "EXCELLENT!" to AccentRed
            totalLines == 3       -> "FANTASTIC!!" to AccentRed
            totalLines == 4       -> "UNBELIEVABLE!" to GoldColor
            totalLines == 5       -> "ULTRA COMBO!" to GoldColor
            newStreak == 6        -> "AMAZING!" to BlockMint
            newStreak == 7        -> "ON FIRE!!" to AccentRed
            newStreak >= 8       -> "UNSTOPPABLE!" to GoldColor
            else                  -> "" to BlockTeal
        }

        val avgRow = if (result.blastingCells.isNotEmpty()) {
            result.blastingCells.map { it.first }.average().toInt().coerceIn(0, BOARD_SIZE - 1)
        } else 3
        val avgCol = if (result.blastingCells.isNotEmpty()) {
            result.blastingCells.map { it.second }.average().toInt().coerceIn(0, BOARD_SIZE - 1)
        } else 3

        val popup = ScorePopup(
            comboText = comboText,
            pointsText = pointsText,
            messageText = msgText,
            comboColor = comboColor,
            pointsColor = pointsColor,
            messageColor = msgColor,
            row = avgRow,
            col = avgCol
        )

        when {
            result.isPerfectClear -> { soundManager.play(SoundType.PERFECT_CLEAR); vibrationManager.vibrateHeavy() }
            result.isCrossBlast   -> { soundManager.play(SoundType.CROSS_BLAST);   vibrationManager.vibrateHeavy() }
            else -> {
                soundManager.play(SoundType.BLAST, 1f + newStreak * 0.1f)
                vibrationManager.vibrateLight()
            }
        }
        if (newStreak >= 3) soundManager.play(SoundType.COMBO)

        setState {
            copy(
                blastingCells = result.blastingCells, currentScore = newScore,
                comboStreak = newStreak, phase = GamePhase.Blasting,
                popupMessages = listOf(popup), sessionStats = newStats
            )
        }
        animateScore(currentState.displayScore, newScore)

        viewModelScope.launch {
            delay(520)
            val clearedBoard = blastLines.clear(board, result.blastingCells)
            val newBest = maxOf(currentState.bestScore, newScore)
            setState {
                copy(
                    board = clearedBoard, blastingCells = emptySet(),
                    phase = GamePhase.Playing, bestScore = newBest,
                    isNewBestScore = newScore > currentState.bestScore && newScore > 0
                )
            }
            saveCurrentState()
            if (tray.all { it == null }) refreshTray(clearedBoard, currentState.difficulty)
            else checkAndHandleGameOver(clearedBoard, tray)
        }
    }

    private fun refreshTray(board: List<List<BoardCell>>, difficulty: Difficulty) {
        val newTray = spawnBlocks(board, difficulty)
        setState { copy(trayBlocks = newTray) }
        saveCurrentState()
        checkAndHandleGameOver(board, newTray)
    }

    private fun checkAndHandleGameOver(board: List<List<BoardCell>>, tray: List<Block?>) {
        if (checkGameOver(board, tray)) startReviveCountdown()
    }

    private fun startReviveCountdown() {
        setState { copy(phase = GamePhase.RevivePrompt, reviveCountdown = 5) }
        reviveJob?.cancel()
        reviveJob = viewModelScope.launch {
            repeat(5) {
                delay(1000)
                val c = currentState.reviveCountdown - 1
                setState { copy(reviveCountdown = c) }
                if (c <= 0) { endGame(); return@launch }
            }
        }
    }

    private fun handleRevive() {
        reviveJob?.cancel()
        if (!currentState.canRevive) { endGame(); return }
        soundManager.play(SoundType.REVIVE)
        val cleared = clearReviveArea(currentState.board)
        val newTray = spawnBlocks(cleared, currentState.difficulty)
        setState { copy(board = cleared, trayBlocks = newTray, phase = GamePhase.Playing, canRevive = true, comboStreak = 0) }
        saveCurrentState()
    }

    private fun clearReviveArea(board: List<List<BoardCell>>): List<List<BoardCell>> {
        val nb = board.map { it.toMutableList() }.toMutableList()
        var bestRow = 0; var bestCol = 0; var bestCount = 0
        for (r in 0..5) for (c in 0..5) {
            val count = (0..2).sumOf { dr -> (0..2).count { dc -> board[r + dr][c + dc].isFilled } }
            if (count > bestCount) { bestCount = count; bestRow = r; bestCol = c }
        }
        for (dr in 0..2) for (dc in 0..2) nb[bestRow + dr][bestCol + dc] = BoardCell()
        return nb
    }

    private fun endGame() {
        reviveJob?.cancel()
        val s = currentState
        viewModelScope.launch {
            clearGameState()
            val newBest = maxOf(s.currentScore, s.bestScore)
            if (s.currentScore > 0) {
                saveScore(ScoreRecord(
                    score = s.currentScore, timestamp = System.currentTimeMillis(),
                    difficulty = s.difficulty.name,
                    linesBlasted = s.sessionStats.linesBlasted, crossBlasts = s.sessionStats.crossBlasts,
                    bestCombo = s.sessionStats.bestCombo, blocksPlaced = s.sessionStats.blocksPlaced
                ))
            }
            soundManager.play(SoundType.GAME_OVER)
            vibrationManager.vibrateHeavy()
            sendEvent(GameUiEvent.NavigateGameOver(s.currentScore, newBest))
        }
    }

    private fun animateScore(from: Int, to: Int) {
        scoreAnimJob?.cancel()
        scoreAnimJob = viewModelScope.launch {
            val diff = to - from
            repeat(20) { i ->
                setState { copy(displayScore = from + diff * (i + 1) / 20) }
                delay(16)
            }
            setState { copy(displayScore = to) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        reviveJob?.cancel(); scoreAnimJob?.cancel(); settingsJob?.cancel()
    }
}

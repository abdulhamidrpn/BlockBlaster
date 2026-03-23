package com.rpn.blockblaster.feature.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.domain.model.BOARD_SIZE
import com.rpn.blockblaster.domain.model.GamePhase
import com.rpn.blockblaster.domain.model.ScorePopup
import com.rpn.blockblaster.feature.game.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.rpn.blockblaster.service.AdManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun GameScreen(
    onGameOver: (Int, Int) -> Unit,
    onHome:     () -> Unit,
    onSettings: () -> Unit = {}
) {
    val vm    = koinViewModel<GameViewModel>()
    val state by vm.state.collectAsState()
    
    val adManager: AdManager = koinInject()
    val context = LocalContext.current
    val activity = context as? Activity

    // ── One-shot events ────────────────────────────────────────────────────
    var shakeSlot by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is GameUiEvent.NavigateGameOver -> onGameOver(event.score, event.best)
                is GameUiEvent.NavigateHome     -> onHome()
                is GameUiEvent.ReplayGame       -> vm.onIntent(GameIntent.StartGame)
                is GameUiEvent.InvalidDrop      -> {
                    shakeSlot = event.slotIndex
                    delay(450)
                    shakeSlot = null
                }
                else -> Unit
            }
        }
    }

    // ── LOCAL drag position – never goes through the ViewModel per pixel ───
    //
    // fingerX / fingerY : raw screen coordinates of the finger
    // activeDragIdx     : which tray slot is being dragged
    // trayY / trayHeight: cached tray bounds for drop detection
    var fingerX       by remember { mutableStateOf(0f) }
    var fingerY       by remember { mutableStateOf(0f) }
    var activeDragIdx by remember { mutableStateOf<Int?>(null) }
    var trayY         by remember { mutableStateOf(0f) }
    var trayHeight    by remember { mutableStateOf(0f) }

    // ── Cell calculation helper ─────────────────────────────────────────────
    //
    // The block appears ABOVE the finger with the finger at its BOTTOM-CENTRE.
    // Formula:
    //   blockRow = round(fingerRow - block.rows)  → finger at bottom of block
    //   blockCol = round(fingerCol - block.cols/2) → finger at centre of block
    //
    // Both are clamped so the block never goes out of bounds.
    // This guarantees every row/column is reachable regardless of block size.
    fun computeSnapCell(fx: Float, fy: Float, idx: Int): Pair<Int, Int> {
        val s   = state
        if (s.cellSize <= 0f) return Pair(0, 0)
        val blk = s.trayBlocks.getOrNull(idx) ?: return Pair(0, 0)

        // Exact physical offset used in FloatingBlock so snap aligns perfectly with visual
        val extraOffsetYPx = s.cellSize * 2f
        val visualTopY = fy - (blk.rows * s.cellSize) - extraOffsetYPx

        val fRow = (visualTopY - s.boardOriginY) / s.cellSize
        val fCol = (fx - s.boardOriginX) / s.cellSize

        val r = fRow.roundToInt().coerceIn(0, BOARD_SIZE - blk.rows)
        val c = (fCol - blk.cols / 2f).roundToInt().coerceIn(0, BOARD_SIZE - blk.cols)
        return Pair(r, c)
    }

    // ── Root Box ────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        StarfieldBackground()

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Score bar ─────────────────────────────────────────────────
            ScoreBar(
                currentScore = state.currentScore,
                bestScore    = state.bestScore,
                displayScore = state.displayScore,
                isNewBest    = state.isNewBestScore,
                onPause      = { vm.onIntent(GameIntent.PauseGame) },
                onSettings   = onSettings
            )

            Spacer(Modifier.height(10.dp))

            // ── Board + combo – centred in remaining space ─────────────────
            Box(
                modifier         = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier            = Modifier.fillMaxHeight()
                ) {
                    // Measure board size for drag coordinate mapping
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.97f)
                            .aspectRatio(1f)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                vm.onIntent(
                                    GameIntent.SetBoardLayout(
                                        x     = pos.x,
                                        y     = pos.y,
                                        width = coords.size.width.toFloat()
                                    )
                                )
                            }
                    ) {
                        GameBoard(
                            board            = state.board,
                            blastingCells    = state.blastingCells,
                            highlightCells   = state.highlightCells,
                            isHighlightValid = state.isHighlightValid,
                            showGrid         = state.settings.showGridLines,
                            isDarkTheme      = state.settings.isDarkTheme,
                            onBoardLayout    = { _, _, _ -> /* already handled above */ },
                            modifier         = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    ComboIndicator(
                        streak   = state.comboStreak,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // ── Block tray ─────────────────────────────────────────────────
            BlockTray(
                blocks         = state.trayBlocks,
                activeDragIdx  = activeDragIdx,
                shakeSlotIndex = shakeSlot,
                onDragStart    = { idx, fx, fy ->
                    activeDragIdx = idx
                    fingerX = fx
                    fingerY = fy
                    vm.onIntent(GameIntent.StartDrag(idx))
                },
                onDragUpdate = { fx, fy ->
                    fingerX = fx
                    fingerY = fy
                    val idx = activeDragIdx ?: return@BlockTray
                    val (r, c) = computeSnapCell(fx, fy, idx)
                    vm.onIntent(GameIntent.UpdateDragCell(r, c))
                },
                onDragEnd    = { fx, fy ->
                    val idx = activeDragIdx ?: return@BlockTray
                    activeDragIdx = null
                    
                    // Check if drop is in the tray area (do not use tolerance to prevent board overlap)
                    val isTrayDrop = fy >= trayY && fy <= (trayY + trayHeight)
                    
                    if (isTrayDrop && trayHeight > 0f) {
                        // Calculate which slot (0, 1, or 2) based on X position
                        // Tray width spans roughly 88% of the screen width
                        val screenWidth = state.cellSize * BOARD_SIZE.toFloat()
                        val trayWidth = screenWidth * 0.88f
                        val trayLeft = state.boardOriginX + (screenWidth - trayWidth) / 2f
                        val slotWidth = trayWidth / 3f
                        val relativeX = fx - trayLeft
                        val targetSlot = (relativeX / slotWidth).toInt().coerceIn(0, 2)
                        
                        // Only trigger swap if target slot is different
                        if (targetSlot != idx) {
                            vm.onIntent(GameIntent.DropBlockInTray(idx, targetSlot))
                        }
                    } else {
                        // Drop on board
                        val (r, c) = computeSnapCell(fx, fy, idx)
                        val dropRow = r.coerceIn(0, BOARD_SIZE - 1)
                        val dropCol = c.coerceIn(0, BOARD_SIZE - 1)
                        vm.onIntent(GameIntent.DropBlock(dropRow, dropCol))
                    }
                },
                onTrayLayout = { y, h ->
                    trayY = y
                    trayHeight = h
                    vm.onIntent(GameIntent.SetTrayLayout(y, h))
                }
            )
        }

        // ── Floating block – follows finger freely ─────────────────────────
        val dragBlock = activeDragIdx?.let { state.trayBlocks.getOrNull(it) }
        if (dragBlock != null && state.cellSize > 0f) {
            FloatingBlock(
                block    = dragBlock,
                screenX  = fingerX,
                screenY  = fingerY,
                cellSize = state.cellSize
            )
        }

        // ── Particle explosion ─────────────────────────────────────────────
        if (state.blastingCells.isNotEmpty()) {
            ExplosionEffect(
                blastingCells = state.blastingCells,
                boardOriginX  = state.boardOriginX,
                boardOriginY  = state.boardOriginY,
                cellSize      = state.cellSize
            )
        }

        // ── Score popups ───────────────────────────────────────────────────
        state.popupMessages.forEach { popup ->
            key(popup.id) {
                ScorePopupToast(
                    popup        = popup,
                    boardOriginX = state.boardOriginX,
                    boardOriginY = state.boardOriginY,
                    cellSize     = state.cellSize,
                    onDismiss    = { vm.onIntent(GameIntent.DismissPopup) }
                )
            }
        }

        // ── Pause overlay ──────────────────────────────────────────────────
        PauseOverlay(
            visible    = state.phase is GamePhase.Paused,
            onResume   = { vm.onIntent(GameIntent.ResumeGame) },
            onRestart  = { vm.onIntent(GameIntent.ReplayGame) },
            onSettings = onSettings,
            onHome     = { vm.onIntent(GameIntent.NavigateHome) }
        )

        // ── Revive dialog ──────────────────────────────────────────────────
        ReviveDialog(
            visible   = state.phase is GamePhase.RevivePrompt,
            countdown = state.reviveCountdown,
            score     = state.currentScore,
            onRevive  = {
                if (activity != null) {
                    vm.onIntent(GameIntent.PauseReviveTimer)
                    adManager.showReviveAd(
                        activity = activity,
                        onRewarded = { vm.onIntent(GameIntent.AcceptRevive) },
                        onFailed = { vm.onIntent(GameIntent.DeclineRevive) },
                        onDismissedUnrewarded = {
                            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                kotlinx.coroutines.delay(500)
                                if (vm.state.value.phase is GamePhase.RevivePrompt) {
                                    vm.onIntent(GameIntent.DeclineRevive)
                                }
                            }
                        }
                    )
                } else {
                    vm.onIntent(GameIntent.DeclineRevive)
                }
            },
            onGiveUp  = { vm.onIntent(GameIntent.DeclineRevive) }
        )
    }
}

// ── Score popup ───────────────────────────────────────────────────────────────

@Composable
private fun ScorePopupToast(
    popup:        ScorePopup,
    boardOriginX: Float,
    boardOriginY: Float,
    cellSize:     Float,
    onDismiss:    () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(popup.id) {
        delay(1000)
        visible = false
        delay(280)
        onDismiss()
    }
    val density = LocalDensity.current
    if (cellSize <= 0f) return
    val x = with(density) { (boardOriginX + popup.col * cellSize).toDp() }
    val y = with(density) { (boardOriginY + popup.row * cellSize - cellSize * 2).toDp() }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(120)) + slideInVertically { 24 },
        exit    = fadeOut(tween(220)) + slideOutVertically { -48 }
    ) {
        Box(modifier = Modifier.absoluteOffset(x = x, y = y)) {
            Text(
                text       = popup.text,
                color      = popup.color,
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ── Starfield background ──────────────────────────────────────────────────────

@Composable
private fun StarfieldBackground() {
    val stars = remember {
        List(45) { Triple((0..100).random() / 100f, (0..100).random() / 100f, (2..5).random()) }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(tween(2800), RepeatMode.Reverse),
        label         = "starAlpha"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { (sx, sy, r) ->
            drawCircle(
                color  = Color.White.copy(alpha = alpha * 0.10f),
                radius = r.toFloat(),
                center = Offset(sx * size.width, sy * size.height)
            )
        }
    }
}

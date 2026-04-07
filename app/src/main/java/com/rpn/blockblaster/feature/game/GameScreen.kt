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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    val playGamesManager: com.rpn.blockblaster.core.play.PlayGamesManager = koinInject()
    val profile by playGamesManager.profileState.collectAsState()
    
    val actualBest = maxOf(state.bestScore, profile?.rawScore?.toInt() ?: 0)
    val actualIsNewBest = state.isNewBestScore && state.currentScore > (profile?.rawScore?.toInt() ?: 0)
    
    val context = LocalContext.current
    val activity = context as? Activity

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

    var fingerX       by remember { mutableStateOf(0f) }
    var fingerY       by remember { mutableStateOf(0f) }
    var activeDragIdx by remember { mutableStateOf<Int?>(null) }
    var trayY         by remember { mutableStateOf(0f) }
    var trayHeight    by remember { mutableStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val isLandscape = maxWidth > maxHeight
        val boxWidth = maxWidth
        val boxHeight = maxHeight
        
        // Reduced offset factor in landscape so the block is closer to finger and bottom can be reached
        val currentOffsetFactor = if (isLandscape) 1.2f else 2.0f

        fun computeSnapCell(fx: Float, fy: Float, idx: Int): Pair<Int, Int> {
            val s   = state
            if (s.cellSize <= 0f) return Pair(0, 0)
            val blk = s.trayBlocks.getOrNull(idx) ?: return Pair(0, 0)

            val extraOffsetYPx = s.cellSize * currentOffsetFactor
            val visualTopY = fy - (blk.rows * s.cellSize) - extraOffsetYPx

            val fRow = (visualTopY - s.boardOriginY) / s.cellSize
            val fCol = (fx - s.boardOriginX) / s.cellSize

            val r = fRow.roundToInt().coerceIn(0, BOARD_SIZE - blk.rows)
            val c = (fCol - blk.cols / 2f).roundToInt().coerceIn(0, BOARD_SIZE - blk.cols)
            return Pair(r, c)
        }

        StarfieldBackground()

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.9f)
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
                                onBoardLayout    = { _, _, _ -> },
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

                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ScoreBar(
                        currentScore = state.currentScore,
                        bestScore    = actualBest,
                        displayScore = state.displayScore,
                        isNewBest    = actualIsNewBest,
                        onPause      = { vm.onIntent(GameIntent.PauseGame) },
                        onSettings   = onSettings
                    )
                    
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
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
                                val (r, c) = computeSnapCell(fx, fy, idx)
                                val dropRow = r.coerceIn(0, BOARD_SIZE - 1)
                                val dropCol = c.coerceIn(0, BOARD_SIZE - 1)
                                vm.onIntent(GameIntent.DropBlock(dropRow, dropCol))
                            },
                            onTrayLayout = { y, h ->
                                trayY = y
                                trayHeight = h
                                vm.onIntent(GameIntent.SetTrayLayout(y, h))
                            }
                        )
                    }
                }
            }
        } else {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScoreBar(
                    currentScore = state.currentScore,
                    bestScore    = actualBest,
                    displayScore = state.displayScore,
                    isNewBest    = actualIsNewBest,
                    onPause      = { vm.onIntent(GameIntent.PauseGame) },
                    onSettings   = onSettings
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier         = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier            = Modifier.fillMaxHeight()
                    ) {
                        val portraitBoardSize = minOf(boxWidth * 0.95f, boxHeight * 0.60f)
                        Box(
                            modifier = Modifier
                                .size(portraitBoardSize)
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
                                onBoardLayout    = { _, _, _ -> },
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
                        val isTrayDrop = fy >= trayY && fy <= (trayY + trayHeight)
                        
                        if (isTrayDrop && trayHeight > 0f) {
                            val trayWidth = boxWidth.value
                            val slotWidth = trayWidth / 3f
                            val targetSlot = (fx / slotWidth).toInt().coerceIn(0, 2)
                            
                            if (targetSlot != idx) {
                                vm.onIntent(GameIntent.DropBlockInTray(idx, targetSlot))
                            }
                        } else {
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
        }

        val dragBlock = activeDragIdx?.let { state.trayBlocks.getOrNull(it) }
        if (dragBlock != null && state.cellSize > 0f) {
            FloatingBlock(
                block    = dragBlock,
                screenX  = fingerX,
                screenY  = fingerY,
                cellSize = state.cellSize,
                offsetFactor = currentOffsetFactor
            )
        }

        if (state.blastingCells.isNotEmpty()) {
            ExplosionEffect(
                blastingCells = state.blastingCells,
                boardOriginX  = state.boardOriginX,
                boardOriginY  = state.boardOriginY,
                cellSize      = state.cellSize
            )
        }

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

        PauseOverlay(
            visible    = state.phase is GamePhase.Paused,
            onResume   = { vm.onIntent(GameIntent.ResumeGame) },
            onRestart  = { vm.onIntent(GameIntent.ReplayGame) },
            onSettings = onSettings,
            onHome     = { vm.onIntent(GameIntent.NavigateHome) }
        )

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
        delay(2200)
        visible = false
        delay(300)
        onDismiss()
    }
    
    val density = LocalDensity.current
    if (cellSize <= 0f) return

    val boardWidthDp = with(density) { (cellSize * BOARD_SIZE).toDp() }
    val boardLeftDp = with(density) { boardOriginX.toDp() }
    val boardTopDp = with(density) { boardOriginY.toDp() }

    val cellYDp = with(density) { (popup.row * cellSize).toDp() }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse),
        label = "scale"
    )

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(150)) + scaleIn(tween(300), initialScale = 0.4f),
        exit    = fadeOut(tween(400)) + slideOutVertically { -200 } + scaleOut(targetScale = 1.5f),
        modifier = Modifier
            .offset(x = boardLeftDp, y = boardTopDp + cellYDp - 40.dp)
            .width(boardWidthDp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!popup.comboText.isNullOrBlank()) {
                val comboParts = popup.comboText.split(" ")
                val mainText = comboParts.getOrNull(0) ?: ""
                val numberText = comboParts.getOrNull(1) ?: ""
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.graphicsLayer { scaleX = scalePulse; scaleY = scalePulse }
                ) {
                    Text(
                        text = mainText,
                        color = popup.comboColor,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            shadow = Shadow(Color.Black.copy(0.8f), Offset(4f, 4f), 8f)
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = numberText,
                        color = popup.comboColor,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 54.sp,
                            shadow = Shadow(Color.Black.copy(0.8f), Offset(5f, 5f), 10f)
                        )
                    )
                }
            }

            var showPoints by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(250); showPoints = true }
            
            AnimatedVisibility(
                visible = showPoints,
                enter = fadeIn(tween(200)) + expandVertically() + scaleIn(initialScale = 0.8f)
            ) {
                Text(
                    text = popup.pointsText,
                    color = popup.pointsColor,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        shadow = Shadow(Color.Black.copy(0.7f), Offset(4f, 4f), 8f)
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            var showCommentary by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(500); showCommentary = true }
            
            AnimatedVisibility(
                visible = showCommentary,
                enter = fadeIn(tween(300)) + slideInVertically { 20 }
            ) {
                Text(
                    text = popup.messageText ?: "",
                    color = popup.messageColor,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(Color.Black.copy(0.9f), Offset(3f, 3f), 6f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

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

@Preview(name = "Phone")
@Preview(name = "Tablet", device = Devices.TABLET)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Composable
private fun GameScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            StarfieldBackground()
        }
    }
}

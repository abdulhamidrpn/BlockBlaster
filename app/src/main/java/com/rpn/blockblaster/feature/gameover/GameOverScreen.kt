package com.rpn.blockblaster.feature.gameover

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.rpn.blockblaster.core.designsystem.AccentRed
import com.rpn.blockblaster.core.designsystem.BlockColors
import com.rpn.blockblaster.core.designsystem.GoldColor
import com.rpn.blockblaster.core.play.PlayGamesManager
import com.rpn.blockblaster.core.play.PlayServicesManager
import com.rpn.blockblaster.feature.game.components.Button3D
import com.rpn.blockblaster.service.AdManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun GameOverScreen(
    finalScore: Int,
    bestScore: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val vm: GameOverViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val adManager: AdManager = koinInject()
    val playGamesManager: PlayGamesManager = koinInject()
    val playServicesManager: PlayServicesManager = koinInject()
    val profile by playGamesManager.profileState.collectAsState()
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(finalScore, bestScore) {
        vm.init(finalScore, bestScore)

        if (activity != null) {
            playGamesManager.submitScore(activity, finalScore)
            if (finalScore >= 1000) {
                playGamesManager.unlockAchievement(activity)
            }
        }
    }

    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is GameOverUiEvent.NavigateGame -> onPlayAgain()
                is GameOverUiEvent.NavigateHome -> onHome()
                is GameOverUiEvent.Share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "I scored ${event.score} in Block Blaster! Can you beat me?\nDownload the game here:\n$playStoreUrl"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Score"))
                }
                is GameOverUiEvent.TriggerReview -> {
                    activity?.let { playServicesManager.requestInAppReview(it) }
                }
            }
        }
    }

    var showTitle by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(50); showTitle = true
        delay(100); showCard = true
        delay(150); showButtons = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        FallingBlocksBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Use Top and manage with spacers for more control
        ) {
            // Reduced initial top space
            Spacer(Modifier.height(46.dp))

            // ── Title ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(400)) + slideInVertically { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GAME OVER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentRed,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayLarge.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = AccentRed.copy(alpha = 0.4f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Text(
                        text = "OUT OF MOVES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Unified Score Summary Card ───────────────────────────────────
            val actualBest = maxOf(state.bestScore, profile?.rawScore?.toInt() ?: 0)
            AnimatedVisibility(
                visible = showCard,
                enter = fadeIn(tween(500)) + slideInVertically { 30 }
            ) {
                SummaryCard(
                    finalScore = state.finalScore,
                    bestScore = actualBest,
                    isNewBest = state.finalScore > actualBest || state.isNewBest
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Action buttons ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(400)) + slideInVertically { 50 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button3D(
                        text = "PLAY AGAIN",
                        onClick = { vm.onIntent(GameOverIntent.PlayAgain) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color = AccentRed,
                        textSize = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button3D(
                            text = "HOME",
                            onClick = { vm.onIntent(GameOverIntent.GoHome) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            color = Color(0xFF1E293B),
                            textSize = 15.sp
                        )
                        Button3D(
                            text = "SHARE",
                            onClick = { vm.onIntent(GameOverIntent.Share) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            color = Color(0xFF334155),
                            textSize = 15.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button3D(
                            text = "LEADERBOARDS",
                            onClick = { activity?.let { playGamesManager.showLeaderboard(it) } },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            color = Color(0xFF0F766E),
                            textSize = 15.sp
                        )
                        Button3D(
                            text = "RATE APP",
                            onClick = {
                                activity?.let {
                                    coroutineScope.launch {
                                        playServicesManager.requestInAppReview(it)
                                        playServicesManager.openPlayStoreForReview()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            color = Color(0xFF5B21B6),
                            textSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        if (state.isNewBest) {
            ConfettiEffect()
        }

        AndroidView(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = adManager.bannerAdUnitId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(
    finalScore: Int,
    bestScore: Int,
    isNewBest: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "summaryGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isNewBest) 0.3f else 0f,
        targetValue = if (isNewBest) 0.8f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isNewBest) Modifier.border(
                    width = 2.dp,
                    color = GoldColor.copy(alpha = glowAlpha),
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // Set to Transparent to match the background column perfectly
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 20.dp), // Reduced vertical padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isNewBest) {
                Text(
                    text = "NEW HIGH SCORE!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldColor,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.height(4.dp))
            } else {
                Text(
                    text = "SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                )
            }

            Text(
                text = finalScore.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = if (isNewBest) GoldColor else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = (if (isNewBest) GoldColor else Color.Black).copy(alpha = 0.2f),
                        offset = Offset(0f, 4f),
                        blurRadius = 6f
                    )
                )
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BEST SCORE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = bestScore.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ConfettiEffect() {
    data class ConfettiParticle(
        val startX: Float,
        val startY: Float,
        val color: Color,
        val size: Float,
        val speed: Float
    )

    val particles = remember {
        List(70) {
            ConfettiParticle(
                startX = (0..1000).random() / 1000f,
                startY = -(0..40).random() / 100f,
                color = BlockColors[(0 until BlockColors.size).random()],
                size = (4..10).random().toFloat(),
                speed = (0.6f..1.4f).random()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val x = p.startX * size.width
            val y = ((p.startY + progress * p.speed) % 1.1f) * size.height
            val alpha = if (y > size.height * 0.85f) ((size.height - y) / (size.height * 0.15f)).coerceIn(0f, 1f) else 0.85f
            if (y > 0f) {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun FallingBlocksBackground() {
    data class BgBlock(val sx: Float, val sy: Float, val color: Color, val speed: Float)

    val blocks = remember {
        List(18) {
            BgBlock(
                sx = (0..100).random() / 100f,
                sy = (0..100).random() / 100f,
                color = BlockColors[(0 until BlockColors.size).random()],
                speed = (0.3f..0.7f).random()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bgFalling")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fallingProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        blocks.forEach { b ->
            val x = b.sx * size.width
            val y = ((b.sy + progress * b.speed) % 1f) * size.height
            drawRoundRect(
                color = b.color.copy(alpha = 0.05f),
                topLeft = Offset(x, y),
                size = Size(20f, 20f),
                cornerRadius = CornerRadius(4f)
            )
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()

@Preview(name = "Phone", showBackground = true)
@Composable
private fun GameOverScreenPreview() {
    MaterialTheme {
        GameOverScreen(
            finalScore = 1234,
            bestScore = 5678,
            onPlayAgain = { },
            onHome = { }
        )
    }
}

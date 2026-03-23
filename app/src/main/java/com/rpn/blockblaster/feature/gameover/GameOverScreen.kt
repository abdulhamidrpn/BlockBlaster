package com.rpn.blockblaster.feature.gameover

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rpn.blockblaster.core.designsystem.AccentRed
import com.rpn.blockblaster.core.designsystem.BlockColors
import com.rpn.blockblaster.core.designsystem.GoldColor
import com.rpn.blockblaster.feature.game.components.Button3D
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.ui.viewinterop.AndroidView
import com.rpn.blockblaster.service.AdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun GameOverScreen(
    finalScore:  Int,
    bestScore:   Int,
    onPlayAgain: () -> Unit,
    onHome:      () -> Unit
) {
    val vm: GameOverViewModel = koinViewModel()
    val state   by vm.state.collectAsState()
    val context = LocalContext.current
    val adManager: AdManager = koinInject()

    LaunchedEffect(finalScore, bestScore) {
        vm.init(finalScore, bestScore)
    }

    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is GameOverUiEvent.NavigateGame -> onPlayAgain()
                is GameOverUiEvent.NavigateHome -> onHome()
                is GameOverUiEvent.Share -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "I scored ${event.score} in Block Blaster! Can you beat me? #BlockBlaster"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Score"))
                }
            }
        }
    }

    // Staggered card entrance animations
    var showTitle   by remember { mutableStateOf(false) }
    var showCard1   by remember { mutableStateOf(false) }
    var showCard2   by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80);  showTitle   = true
        delay(160); showCard1   = true
        delay(160); showCard2   = true
        delay(160); showButtons = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Animated falling blocks background
        FallingBlocksBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(40.dp))

            // ── Title ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showTitle,
                enter   = fadeIn(tween(400)) + slideInVertically { -40 }
            ) {
                Text(
                    text          = "GAME OVER",
                    fontSize      = 40.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = AccentRed,
                    letterSpacing = 4.sp,
                    textAlign     = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Your Score card ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = showCard1,
                enter   = fadeIn(tween(400)) + slideInVertically { 60 }
            ) {
                ScoreCard(
                    label     = "YOUR SCORE",
                    value     = state.finalScore,
                    isNewBest = state.isNewBest,
                    valueColor = if (state.isNewBest) GoldColor
                                 else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Best Score card ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = showCard2,
                enter   = fadeIn(tween(400)) + slideInVertically { 80 }
            ) {
                ScoreCard(
                    label      = "BEST SCORE",
                    value      = state.bestScore,
                    isNewBest  = false,
                    valueColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Action buttons ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = showButtons,
                enter   = fadeIn(tween(400)) + slideInVertically { 100 }
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button3D(
                        text     = "PLAY AGAIN",
                        onClick  = { vm.onIntent(GameOverIntent.PlayAgain) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        color    = AccentRed,
                        textSize = 18.sp
                    )

                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button3D(
                            text     = "HOME",
                            onClick  = { vm.onIntent(GameOverIntent.GoHome) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            color    = Color(0xFF0F3460),
                            textSize = 16.sp
                        )
                        Button3D(
                            text     = "SHARE",
                            onClick  = { vm.onIntent(GameOverIntent.Share) },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            color    = Color(0xFF2D2D50),
                            textSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }

        // Confetti overlay only when new best score
        if (state.isNewBest) {
            ConfettiEffect()
        }

        // ── Banner Ad ──────────────────────────────────────────────────────────
        AndroidView(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
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

// ── Score Card ────────────────────────────────────────────────────────────────

@Composable
private fun ScoreCard(
    label:      String,
    value:      Int,
    isNewBest:  Boolean,
    valueColor: Color
) {
    // FIX: infiniteTransition.animateFloat must use infiniteRepeatable(), NOT tween() directly.
    // tween() produces TweenSpec<T> which is incompatible with InfiniteRepeatableSpec<T>.
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isNewBest) 0.35f else 0f,
        targetValue  = if (isNewBest) 1f    else 0f,
        animationSpec = infiniteRepeatable(          // <-- CORRECT: wraps tween in infiniteRepeatable
            animation   = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .then(
                if (isNewBest) Modifier.border(
                    width = 2.dp,
                    color = GoldColor.copy(alpha = glowAlpha),
                    shape = RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isNewBest) {
                Text(
                    text          = "NEW BEST!",
                    fontSize      = 13.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = GoldColor,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(4.dp))
            }

            Text(
                text          = label,
                fontSize      = 11.sp,
                fontWeight    = FontWeight.SemiBold,
                color         = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 2.5.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text       = value.toString(),
                fontSize   = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = valueColor
            )
        }
    }
}

// ── Confetti (new best score celebration) ────────────────────────────────────

@Composable
private fun ConfettiEffect() {
    data class ConfettiParticle(
        val startX: Float,
        val startY: Float,
        val color:  Color,
        val size:   Float,
        val speed:  Float
    )

    val particles = remember {
        List(70) {
            ConfettiParticle(
                startX = (0..1000).random() / 1000f,
                startY = -(0..40).random()  / 100f,
                color  = BlockColors[(0 until BlockColors.size).random()],
                size   = (4..10).random().toFloat(),
                speed  = (0.6f..1.4f).random()
            )
        }
    }

    // FIX: Use infiniteRepeatable(tween(...)) — NOT tween() directly
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val x     = p.startX * size.width
            val y     = ((p.startY + progress * p.speed) % 1.1f) * size.height
            val alpha = if (y > size.height * 0.85f) ((size.height - y) / (size.height * 0.15f)).coerceIn(0f, 1f) else 0.85f
            if (y > 0f) {
                drawCircle(
                    color  = p.color.copy(alpha = alpha),
                    radius = p.size,
                    center = Offset(x, y)
                )
            }
        }
    }
}

// ── Falling blocks background ─────────────────────────────────────────────────

@Composable
private fun FallingBlocksBackground() {
    data class BgBlock(val sx: Float, val sy: Float, val color: Color, val speed: Float)

    val blocks = remember {
        List(18) {
            BgBlock(
                sx    = (0..100).random() / 100f,
                sy    = (0..100).random() / 100f,
                color = BlockColors[(0 until BlockColors.size).random()],
                speed = (0.3f..0.7f).random()
            )
        }
    }

    // FIX: infiniteRepeatable(tween(...)) — correct usage
    val infiniteTransition = rememberInfiniteTransition(label = "bgFalling")
    val progress by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fallingProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        blocks.forEach { b ->
            val x = b.sx * size.width
            val y = ((b.sy + progress * b.speed) % 1f) * size.height
            drawRoundRect(
                color        = b.color.copy(alpha = 0.07f),
                topLeft      = Offset(x, y),
                size         = Size(22f, 22f),
                cornerRadius = CornerRadius(4f)
            )
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()

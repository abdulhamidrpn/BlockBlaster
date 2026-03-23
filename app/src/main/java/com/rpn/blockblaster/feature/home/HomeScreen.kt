package com.rpn.blockblaster.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.*
import com.rpn.blockblaster.feature.game.components.Button3D
import com.rpn.blockblaster.feature.home.components.AppTitle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(onPlay: () -> Unit, onSettings: () -> Unit) {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is HomeUiEvent.NavigateGame     -> onPlay()
                is HomeUiEvent.NavigateSettings -> onSettings()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ParticleBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(48.dp))
            AppTitle()
            Spacer(Modifier.weight(0.3f))
            BestScoreCard(bestScore = state.bestScore)
            Spacer(Modifier.weight(0.3f))
            MiniBoardPreview()
            Spacer(Modifier.weight(0.4f))

            Button3D(
                text     = "▶  PLAY",
                onClick  = { vm.onIntent(HomeIntent.NavigateToGame) },
                color    = AccentRed,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                textSize = 22.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button3D(
                    text     = "⚙  Settings",
                    onClick  = { vm.onIntent(HomeIntent.NavigateToSettings) },
                    color    = Color(0xFF2D2D50),
                    modifier = Modifier.weight(1f).height(52.dp),
                    textSize = 16.sp
                )
                Button3D(
                    text     = "🏆  Best",
                    onClick  = { },
                    color    = Color(0xFF2D2D50),
                    modifier = Modifier.weight(1f).height(52.dp),
                    textSize = 16.sp
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BestScoreCard(bestScore: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label         = "glowAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, GoldColor.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👑  BEST SCORE", fontSize = 14.sp, color = GoldColor,
                letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(bestScore.toString(), fontSize = 38.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun MiniBoardPreview() {
    val colors  = listOf(BlockCoral, BlockTeal, BlockGold, BlockLavender)
    val pattern = listOf(listOf(0,1,0,1), listOf(1,0,1,0), listOf(0,1,0,1), listOf(1,0,1,0))
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) { kotlinx.coroutines.delay(400); tick++ }
    }
    Row(horizontalArrangement = Arrangement.Center) {
        repeat(4) { col ->
            Column {
                repeat(4) { row ->
                    val colorIdx   = pattern[row][col]
                    val showFilled = ((tick + row + col) % 5) < 3
                    Box(
                        modifier = Modifier
                            .size(18.dp).padding(1.dp)
                            .background(
                                if (showFilled) colors[colorIdx].copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticleBackground() {
    val particles = remember {
        List(25) { Triple((0..100).random() / 100f, (0..100).random() / 100f, BlockColors[(0 until BlockColors.size).random()]) }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val progress by infiniteTransition.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
        label         = "particleProgress"
    )
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (sx, sy, color) ->
            val x = sx * size.width
            val y = ((sy + progress) % 1f) * size.height
            drawCircle(color = color.copy(alpha = 0.12f), radius = 4f, center = Offset(x, y))
        }
    }
}

package com.rpn.blockblaster.feature.home

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import coil3.compose.AsyncImage
import com.rpn.blockblaster.core.designsystem.*
import com.rpn.blockblaster.core.play.PlayGamesManager
import com.rpn.blockblaster.core.play.PlayGamesProfile
import com.rpn.blockblaster.feature.game.components.Button3D
import com.rpn.blockblaster.feature.home.components.AppTitle
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun HomeScreen(onPlay: () -> Unit, onSettings: () -> Unit) {
    val vm: HomeViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val playGamesManager: PlayGamesManager = koinInject()
    val profile by playGamesManager.profileState.collectAsState()
    val activity = context as? Activity

    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is HomeUiEvent.NavigateGame     -> onPlay()
                is HomeUiEvent.NavigateSettings -> onSettings()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (playGamesManager.isAuthenticated) {
            activity?.let { playGamesManager.fetchPlayerProfile(it) }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isLandscape = maxWidth > maxHeight
        ParticleBackground()
        
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .windowInsetsPadding(WindowInsets.systemBars),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1.1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppTitle()
                    Spacer(Modifier.height(8.dp))
                    MiniBoardPreview()
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val actualBest = maxOf(state.bestScore, profile?.rawScore?.toInt() ?: 0)
                    if (profile != null) {
                        PlayerProfileCard(profile = profile!!, bestScore = actualBest)
                    } else {
                        BestScoreCard(bestScore = actualBest)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button3D(
                        text     = "▶  PLAY",
                        onClick  = { vm.onIntent(HomeIntent.NavigateToGame) },
                        color    = AccentRed,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        textSize = 20.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button3D(
                            text     = "⚙",
                            onClick  = { vm.onIntent(HomeIntent.NavigateToSettings) },
                            color    = Color(0xFF2D2D50),
                            modifier = Modifier.weight(1f).height(48.dp),
                            textSize = 18.sp
                        )
                        Button3D(
                            text     = "🏆",
                            onClick  = { activity?.let { playGamesManager.showLeaderboard(it) } },
                            color    = Color(0xFF2D2D50),
                            modifier = Modifier.weight(1f).height(48.dp),
                            textSize = 18.sp
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(24.dp))
                AppTitle()

                
                Spacer(Modifier.weight(1f))

                val actualBest = maxOf(state.bestScore, profile?.rawScore?.toInt() ?: 0)
                if (profile != null) {
                    PlayerProfileCard(profile = profile!!, bestScore = actualBest)
                } else {
                    BestScoreCard(bestScore = actualBest)
                }
                Spacer(Modifier.height(32.dp))
                MiniBoardPreview()

                Spacer(Modifier.weight(1f))

                Button3D(
                    text     = "▶  PLAY",
                    onClick  = { vm.onIntent(HomeIntent.NavigateToGame) },
                    color    = AccentRed,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    textSize = 22.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button3D(
                        text     = "SETTINGS",
                        onClick  = { vm.onIntent(HomeIntent.NavigateToSettings) },
                        color    = Color(0xFF2D2D50),
                        modifier = Modifier.weight(1f).height(52.dp),
                        textSize = 14.sp
                    )
                    Button3D(
                        text     = "LEADERBOARD",
                        onClick  = { activity?.let { playGamesManager.showLeaderboard(it) } },
                        color    = Color(0xFF2D2D50),
                        modifier = Modifier.weight(1.3f).height(52.dp),
                        textSize = 14.sp
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}


@Composable
private fun BestScoreCard(bestScore: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldColor.copy(alpha = glowAlpha), RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("BEST SCORE", fontSize = 11.sp, color = GoldColor.copy(alpha = 0.8f),
                letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(bestScore.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PlayerProfileCard(profile: PlayGamesProfile, bestScore: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldColor.copy(alpha = glowAlpha), RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profile.avatarUrl != null) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = "Player Avatar",
                modifier = Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.displayName.firstOrNull()?.toString() ?: "?", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(profile.displayName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            if (profile.rank != null) {
                Text("Rank: ${profile.rank}", fontSize = 14.sp, color = GoldColor.copy(alpha = 0.9f))
            } else {
                Text("Connected", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("BEST", fontSize = 11.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
            Text(bestScore.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface)
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
                            .size(12.dp).padding(1.dp)
                            .background(
                                if (showFilled) colors[colorIdx].copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                RoundedCornerShape(2.dp)
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
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing)),
        label         = "particleProgress"
    )
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (sx, sy, color) ->
            val x = sx * size.width
            val y = ((sy + progress) % 1f) * size.height
            drawCircle(color = color.copy(alpha = 0.08f), radius = 3f, center = Offset(x, y))
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()

@Preview(name = "Phone", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onPlay = {}, onSettings = {})
    }
}

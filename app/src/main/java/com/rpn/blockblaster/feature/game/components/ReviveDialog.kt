package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.*
import androidx.compose.foundation.Canvas

@Composable
fun ReviveDialog(
    visible:    Boolean,
    countdown:  Int,
    score:      Int,
    onRevive:   () -> Unit,
    onGiveUp:   () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn() + slideInVertically { it / 2 },
        exit    = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(0.88f),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("💀", fontSize = 48.sp)
                    Text("GAME OVER?", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface, letterSpacing = 2.sp)
                    Text("Score: $score", fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.7f))

                    // Countdown ring
                    CountdownRing(countdown = countdown, total = 5)

                    Button3D(
                        text     = "🎬  REVIVE",
                        onClick  = onRevive,
                        color    = AccentRed,
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    )
                    TextButton(onClick = onGiveUp) {
                        Text("Give Up", color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                            fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownRing(countdown: Int, total: Int) {
    val fraction = countdown.toFloat() / total.toFloat()
    val animFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(900, easing = LinearEasing),
        label         = "countdown"
    )
    val ringColor = when {
        fraction > 0.6f -> Color(0xFF4ECDC4)
        fraction > 0.3f -> GoldColor
        else            -> AccentRed
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val stroke = Stroke(width = 6f, cap = StrokeCap.Round)
            drawArc(
                color      = ringColor.copy(alpha = 0.2f),
                startAngle = -90f, sweepAngle = 360f,
                useCenter  = false, style = stroke
            )
            drawArc(
                color      = ringColor,
                startAngle = -90f,
                sweepAngle = animFraction * 360f,
                useCenter  = false, style = stroke
            )
        }
        Text(
            text       = countdown.toString(),
            fontSize   = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = ringColor
        )
    }
}

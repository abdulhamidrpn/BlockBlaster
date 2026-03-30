package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.AccentRed
import com.rpn.blockblaster.core.designsystem.GoldColor

@Composable
fun ScoreBar(
    currentScore: Int,
    bestScore:    Int,
    displayScore: Int,
    isNewBest:    Boolean,
    onPause:      () -> Unit,
    onSettings:   () -> Unit,
    modifier:     Modifier = Modifier
) {
    val bestLabelColor by animateColorAsState(
        targetValue   = if (isNewBest) GoldColor else MaterialTheme.colorScheme.onSurface.copy(0.55f),
        animationSpec = tween(400),
        label         = "bestLabel"
    )
    val bestValueColor by animateColorAsState(
        targetValue   = if (isNewBest) GoldColor else MaterialTheme.colorScheme.onSurface.copy(0.85f),
        animationSpec = tween(400),
        label         = "bestValue"
    )

    val inf = rememberInfiniteTransition(label = "scoreBarAnim")
    val glowAlpha by inf.animateFloat(
        initialValue  = if (isNewBest) 0.3f else 0f,
        targetValue   = if (isNewBest) 1f   else 0f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label         = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                )
            )
            .then(
                if (isNewBest) Modifier.border(
                    width = 1.5.dp,
                    color = GoldColor.copy(alpha = glowAlpha * 0.6f)
                ) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameIconButton(icon = Icons.Default.Pause, desc = "Pause", onClick = onPause)

            Spacer(Modifier.width(10.dp))

            ScorePill(
                label    = "SCORE",
                value    = displayScore,
                isMain   = true,
                color    = AccentRed,
                modifier = Modifier.weight(1.1f)
            )

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            )

            Spacer(Modifier.width(10.dp))

            BestScorePill(
                value      = bestScore,
                isNewBest  = isNewBest,
                labelColor = bestLabelColor,
                valueColor = bestValueColor,
                glowAlpha  = glowAlpha,
                modifier   = Modifier.weight(1f)
            )

            Spacer(Modifier.width(10.dp))

            GameIconButton(icon = Icons.Default.Settings, desc = "Settings", onClick = onSettings)
        }
    }
}

@Composable
private fun ScorePill(
    label:    String,
    value:    Int,
    isMain:   Boolean,
    color:    Color,
    modifier: Modifier = Modifier
) {
    var prevValue by remember { mutableStateOf(value) }
    val popScale by animateFloatAsState(
        targetValue   = if (value != prevValue) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        finishedListener = { prevValue = value },
        label = "popScale"
    )
    LaunchedEffect(value) { prevValue = value }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text          = label,
                fontSize      = 9.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = color,
                letterSpacing = 1.5.sp
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text       = value.toString(),
            fontSize   = if (isMain) 30.sp else 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onSurface,
            modifier   = Modifier.graphicsLayer { scaleX = popScale; scaleY = popScale }
        )
    }
}

@Composable
private fun BestScorePill(
    value:      Int,
    isNewBest:  Boolean,
    labelColor: Color,
    valueColor: Color,
    glowAlpha:  Float,
    modifier:   Modifier = Modifier
) {
    val crownScale by animateFloatAsState(
        targetValue   = if (isNewBest) 1.3f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "crownScale"
    )

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isNewBest) {
                Text(
                    text     = "👑 ",
                    fontSize = 11.sp,
                    modifier = Modifier
                        .graphicsLayer { scaleX = crownScale; scaleY = crownScale }
                        .padding(end = 3.dp)
                )
            }
            Text(
                text          = "BEST",
                fontSize      = 9.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = labelColor,
                letterSpacing = 1.5.sp
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text       = value.toString(),
            fontSize   = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = valueColor
        )
        if (isNewBest) {
            Text(
                text          = "NEW BEST!",
                fontSize      = 8.sp,
                fontWeight    = FontWeight.Bold,
                color         = GoldColor.copy(alpha = glowAlpha),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun GameIconButton(
    icon:    ImageVector,
    desc:    String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "iconScale"
    )
    IconButton(
        onClick           = onClick,
        interactionSource = interactionSource,
        modifier          = Modifier
            .size(42.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = desc,
            tint               = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier           = Modifier.size(20.dp)
        )
    }
}
package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.shadow

@Composable
fun Button3D(
    text:     String,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
    color:    Color    = Color(0xFFE94560),
    enabled:  Boolean  = true,
    textSize: TextUnit = 18.sp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed || !enabled) 0.dp else 4.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn3d"
    )
    val shadowColor  = color.shadow()
    val faceColor    = if (enabled) color else color.copy(alpha = 0.5f)
    val shape        = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = enabled,
                onClick           = onClick
            )
    ) {
        // Shadow layer (bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(shadowColor, shape)
        )
        // Face layer (top, offsets upward to show shadow below)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = -offsetY)
                .background(faceColor, shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = text,
                fontSize   = textSize,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                letterSpacing = 1.sp,
                modifier   = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

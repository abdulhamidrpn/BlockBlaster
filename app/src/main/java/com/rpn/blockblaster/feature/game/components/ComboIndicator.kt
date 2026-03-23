package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.GoldColor

@Composable
fun ComboIndicator(streak: Int, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = when {
            streak >= 5 -> Color(0xFFFF4444)
            streak >= 3 -> Color(0xFFFF8C00)
            streak >= 2 -> GoldColor
            else        -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        },
        animationSpec = tween(300), label = "comboColor"
    )
    val scale by animateFloatAsState(
        targetValue   = if (streak >= 2) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "comboScale"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        val mult = 1f + (streak * 0.2f)
        Text(
            text          = if (streak >= 2) "COMBO  ×${String.format("%.1f", mult)}" else "COMBO  ×1.0",
            fontSize      = 13.sp,
            fontWeight    = FontWeight.Bold,
            color         = color,
            letterSpacing = 1.sp
        )
    }
}

package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpn.blockblaster.domain.model.Block

@Preview
@Composable
private fun PreviewBlockTray() {
    BlockTray(
        blocks = listOf(null, null, null),
        activeDragIdx = 0,
        shakeSlotIndex = null,
        onDragStart = { _, _, _ -> },
        onDragUpdate = { _, _ -> },
        onDragEnd = { _, _ -> }
    )
}

@Composable
fun BlockTray(
    blocks: List<Block?>,
    activeDragIdx: Int?,
    shakeSlotIndex: Int?,
    onDragStart: (idx: Int, screenX: Float, screenY: Float) -> Unit,
    onDragUpdate: (screenX: Float, screenY: Float) -> Unit,
    onDragEnd: (screenX: Float, screenY: Float) -> Unit,
    onTrayLayout: (y: Float, height: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                onTrayLayout(pos.y, coords.size.height.toFloat())
            }
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            )
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { idx ->
            TraySlot(
                block = blocks.getOrNull(idx),
                isDragging = activeDragIdx == idx,
                shouldShake = shakeSlotIndex == idx,
                onDragStart = { x, y -> onDragStart(idx, x, y) },
                onDragUpdate = onDragUpdate,
                onDragEnd = onDragEnd,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TraySlot(
    block: Block?,
    isDragging: Boolean,
    shouldShake: Boolean,
    onDragStart: (x: Float, y: Float) -> Unit,
    onDragUpdate: (x: Float, y: Float) -> Unit,
    onDragEnd: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(shouldShake) {
        if (!shouldShake) return@LaunchedEffect
        shakeAnim.snapTo(0f)
        shakeAnim.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 400
                -16f at 50 with LinearEasing
                16f at 110 with LinearEasing
                -12f at 170 with LinearEasing
                12f at 230 with LinearEasing
                -8f at 290 with LinearEasing
                8f at 340 with LinearEasing
                0f at 400
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .graphicsLayer { translationX = shakeAnim.value },
        contentAlignment = Alignment.Center
    ) {
        when {
            block != null -> DraggableBlock(
                block = block,
                index = 0, // slot identity handled by onDragStart closure
                enabled = true,
                onDragStart = onDragStart,
                onDragUpdate = onDragUpdate,
                onDragEnd = onDragEnd,
                onDragCancel = {},
                modifier = Modifier.fillMaxWidth()
            )

            block == null -> UsedSlot()
        }
    }
}

@Composable
private fun UsedSlot() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                RoundedCornerShape(10.dp)
            )
    )
}

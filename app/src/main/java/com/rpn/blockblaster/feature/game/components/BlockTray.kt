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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.tooling.preview.Devices
import com.rpn.blockblaster.domain.model.Block

@Preview(name = "Phone")
@Preview(name = "Tablet", device = Devices.TABLET)
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
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(28.dp)
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

    var rootPos by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .graphicsLayer { translationX = shakeAnim.value }
            .onGloballyPositioned { coords -> rootPos = coords.positionInRoot() }
            .pointerInput(block != null) {
                if (block == null) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    var dragPos = rootPos + down.position
                    onDragStart(dragPos.x, dragPos.y)

                    var active = true
                    while (active) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }

                        if (change == null) {
                            active = false
                            onDragEnd(dragPos.x, dragPos.y)
                            break
                        }

                        if (change.changedToUp() || change.changedToUpIgnoreConsumed() || !change.pressed) {
                            active = false
                            onDragEnd(dragPos.x, dragPos.y)
                            break
                        }

                        val posChange = change.positionChange()
                        if (posChange != Offset.Zero) {
                            change.consume()
                            dragPos += posChange
                            onDragUpdate(dragPos.x, dragPos.y)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isDragging) 0.85f else 1f,
            animationSpec = tween(150),
            label = "scaleAnim"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isDragging) 0.3f else 1f,
            animationSpec = tween(80),
            label = "alphaAnim"
        )

        when {
            block != null -> {
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                ) {
                    BlockPreview(block = block, cellDp = 24)
                }
            }

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

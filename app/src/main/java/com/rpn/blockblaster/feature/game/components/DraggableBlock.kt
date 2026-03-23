package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rpn.blockblaster.domain.model.Block

@Composable
fun DraggableBlock(
    block:        Block,
    index:        Int,
    enabled:      Boolean = true,
    onDragStart:  (screenX: Float, screenY: Float) -> Unit,
    onDragUpdate: (screenX: Float, screenY: Float) -> Unit,
    onDragEnd:    (screenX: Float, screenY: Float) -> Unit,
    onDragCancel: () -> Unit,
    modifier:     Modifier = Modifier
) {
    var rootPos  by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }

    val trayAlpha by animateFloatAsState(
        targetValue   = if (dragging) 0f else 1f,
        animationSpec = tween(80),
        label         = "trayAlpha"
    )

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                rootPos = coords.positionInRoot()
            }
            .graphicsLayer { alpha = trayAlpha }
            .pointerInput(index, enabled) {
                if (!enabled) return@pointerInput

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    dragging = true

                    var dragPos = rootPos + down.position
                    onDragStart(dragPos.x, dragPos.y)

                    var active = true
                    while (active) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }

                        if (change == null) {
                            active = false
                            dragging = false
                            onDragCancel()
                            break
                        }

                        if (change.changedToUp() || change.changedToUpIgnoreConsumed() || !change.pressed) {
                            active = false
                            dragging = false
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
        BlockPreview(block = block, cellDp = 22)
    }
}

// ── Static block cell grid (tray preview) ────────────────────────────────────

@Composable
fun BlockPreview(block: Block, cellDp: Int = 18) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        block.shape.forEach { rowCells ->
            Row {
                rowCells.forEach { filled ->
                    Box(
                        modifier = Modifier
                            .size(cellDp.dp)
                            .padding(1.5.dp)
                            .then(
                                if (filled) {
                                    Modifier.drawBehind {
                                        val rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                                        draw3DBlockCell(rect, block.color)
                                    }
                                } else Modifier
                            )
                    )
                }
            }
        }
    }
}

// ── Floating ghost block – follows finger accurately ────────────────────────────

@Composable
fun FloatingBlock(
    block:        Block,
    screenX:      Float,
    screenY:      Float,
    cellSize:     Float
) {
    if (cellSize <= 0f) return

    val density = LocalDensity.current
    val cellDp: Dp = with(density) { cellSize.toDp() }

    val blockWidthPx  = block.cols * cellSize
    val blockHeightPx = block.rows * cellSize

    // Extra offset so the block is fully visible above the finger
    val extraOffsetYPx = cellSize * 2f

    // Center block horizontally on finger, position bottom of block above finger
    val offsetXDp = with(density) { (screenX - blockWidthPx / 2f).toDp() }
    val offsetYDp = with(density) { (screenY - blockHeightPx - extraOffsetYPx).toDp() }

    Box(
        modifier = Modifier
            .absoluteOffset(x = offsetXDp, y = offsetYDp)
            .graphicsLayer {
                shadowElevation = 20f
                alpha           = 0.93f
            }
    ) {
        Column {
            block.shape.forEach { rowCells ->
                Row {
                    rowCells.forEach { filled ->
                        Box(
                            modifier = Modifier
                                .size(cellDp)
                                .padding(1.5.dp)
                                .then(
                                    if (filled) {
                                        Modifier
                                            .graphicsLayer { shadowElevation = 15f }
                                            .drawBehind {
                                                val rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                                                draw3DBlockCell(rect, block.color)
                                            }
                                    } else Modifier
                                )
                        )
                    }
                }
            }
        }
    }
}

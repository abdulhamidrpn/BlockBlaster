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
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rpn.blockblaster.domain.model.Block

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
    cellSize:     Float,
    offsetFactor: Float = 2f // Added offsetFactor to control vertical offset
) {
    if (cellSize <= 0f) return

    val density = LocalDensity.current
    val cellDp: Dp = with(density) { cellSize.toDp() }

    val blockWidthPx  = block.cols * cellSize
    val blockHeightPx = block.rows * cellSize

    // Extra offset so the block is fully visible above the finger
    val extraOffsetYPx = cellSize * offsetFactor

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

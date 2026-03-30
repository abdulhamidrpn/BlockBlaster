package com.rpn.blockblaster.feature.game.components

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rpn.blockblaster.core.designsystem.BoardBackground
import com.rpn.blockblaster.core.designsystem.BoardBackgroundLight
import com.rpn.blockblaster.core.designsystem.GridLine
import com.rpn.blockblaster.core.designsystem.GridLineLight
import com.rpn.blockblaster.domain.model.BOARD_SIZE
import com.rpn.blockblaster.domain.model.BoardCell
import com.rpn.blockblaster.domain.model.emptyBoard

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun PreviewGameBoard() {
    MaterialTheme() {
        GameBoard(
            board = emptyBoard(),
            blastingCells = emptySet(),
            highlightCells = emptySet(),
            isHighlightValid = true,
            isDarkTheme = false,
            modifier = Modifier
        )
    }
}

@Composable
fun GameBoard(
    board: List<List<BoardCell>>,
    blastingCells: Set<Pair<Int, Int>>,
    highlightCells: Set<Pair<Int, Int>>,
    isHighlightValid: Boolean,
    showGrid: Boolean = true,
    isDarkTheme: Boolean = true,
    onBoardLayout: (x: Float, y: Float, cellSize: Float) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blast")
    val blastAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(200), RepeatMode.Reverse),
        label = "blastAlpha"
    )
    val highlightColor = if (isHighlightValid) Color(0x5500FF88) else Color(0x55FF4444)

    BoxWithConstraints(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val cellSize = coords.size.width / BOARD_SIZE.toFloat()
                onBoardLayout(pos.x, pos.y, cellSize)
            }
    ) {
        val cellSize = constraints.maxWidth / BOARD_SIZE.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardColor =
                if (isDarkTheme) BoardBackground else BoardBackgroundLight
            val gridColor =
                if (isDarkTheme) GridLine else GridLineLight

            drawRoundRect(
                color = boardColor,
                cornerRadius = CornerRadius(8.dp.toPx()),
                size = this.size
            )
            for (row in 0 until BOARD_SIZE) {
                for (col in 0 until BOARD_SIZE) {
                    val cell = board[row][col]
                    val left = col * cellSize
                    val top = row * cellSize
                    val rect = Rect(left + 1f, top + 1f, left + cellSize - 1f, top + cellSize - 1f)
                    val isBlast = blastingCells.contains(Pair(row, col))
                    val isHigh = highlightCells.contains(Pair(row, col))

                    when {
                        isBlast -> drawBlastingCell(rect, blastAlpha, cell.color)
                        isHigh -> drawHighlightCell(rect, highlightColor, cell.color)
                        cell.isFilled -> draw3DBlockCell(rect, cell.color ?: Color.White)
                        showGrid -> drawEmptyCell(rect, gridColor)
                    }
                }
            }
            if (showGrid) drawGrid(cellSize, BOARD_SIZE, gridColor)
        }
    }
}

fun DrawScope.draw3DBlockCell(rect: Rect, color: Color) {
    val cornerPx = 6.dp.toPx()
    val r = CornerRadius(cornerPx)
    val inset = 5f

    // Create base path for clipping so no shadows/highlights bleed out of the rounded corners
    val basePath = Path().apply {
        addRoundRect(RoundRect(rect, r.x, r.y))
    }

    clipPath(basePath) {
        // 1. Draw solid base
        drawRect(color = color, topLeft = rect.topLeft, size = rect.size)

        // 2. Top-left highlight (Bevel)
        val highlightColor = Color.White.copy(alpha = 0.5f)
        val pathTopLeft = Path().apply {
            moveTo(rect.left, rect.bottom)
            lineTo(rect.left, rect.top)
            lineTo(rect.right, rect.top)
            lineTo(rect.right - inset, rect.top + inset)
            lineTo(rect.left + inset, rect.top + inset)
            lineTo(rect.left + inset, rect.bottom - inset)
            close()
        }
        drawPath(pathTopLeft, highlightColor)

        // 3. Bottom-right shadow (Bevel)
        val darkBevel = Color.Black.copy(alpha = 0.35f)
        val pathBottomRight = Path().apply {
            moveTo(rect.right, rect.top)
            lineTo(rect.right, rect.bottom)
            lineTo(rect.left, rect.bottom)
            lineTo(rect.left + inset, rect.bottom - inset)
            lineTo(rect.right - inset, rect.bottom - inset)
            lineTo(rect.right - inset, rect.top + inset)
            close()
        }
        drawPath(pathBottomRight, darkBevel)

        // 4. Inner vibrant gradient for a 3D pop effect
        val innerRect =
            Rect(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
        val brightInner = Color(
            red = (color.red * 1.35f).coerceAtMost(1f),
            green = (color.green * 1.35f).coerceAtMost(1f),
            blue = (color.blue * 1.35f).coerceAtMost(1f)
        )
        val innerGradient = Brush.radialGradient(
            colors = listOf(brightInner.copy(alpha = 0.65f), Color.Transparent),
            center = Offset(
                innerRect.left + innerRect.width * 0.35f,
                innerRect.top + innerRect.height * 0.35f
            ),
            radius = innerRect.width * 1.2f
        )
        drawRect(
            brush = innerGradient,
            topLeft = innerRect.topLeft,
            size = innerRect.size
        )

        // 5. Glossy top reflection highlight
        val glossHeight = rect.height * 0.45f
        val glossRect = Rect(rect.left, rect.top, rect.right, rect.top + glossHeight)
        val glossGradient = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0.05f)),
            startY = glossRect.top,
            endY = glossRect.bottom
        )
        val glossPath = Path().apply {
            addRoundRect(RoundRect(glossRect, r.x, r.y))
        }
        drawPath(glossPath, glossGradient)
    }
}

private fun DrawScope.drawEmptyCell(rect: Rect, gridColor: Color) {
    drawRoundRect(
        color = gridColor.copy(alpha = 0.9f),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(width = 3.0f)
    )
}

private fun DrawScope.drawHighlightCell(rect: Rect, highlightColor: Color, existingColor: Color?) {
    val r = CornerRadius(3.dp.toPx())
    if (existingColor != null) drawRoundRect(existingColor, rect.topLeft, rect.size, r)
    drawRoundRect(highlightColor, rect.topLeft, rect.size, r)
}

fun DrawScope.drawBlastingCell(rect: Rect, alpha: Float, color: Color?) {
    val blastColor = color ?: Color.White
    // Glowing intense cell
    drawRoundRect(
        color = blastColor.copy(alpha = alpha.coerceIn(0f, 1f)),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(6.dp.toPx())
    )

    // Outer glow halo
    val amount = rect.width * (0.15f + alpha * 0.35f)
    val extraRect =
        Rect(rect.left - amount, rect.top - amount, rect.right + amount, rect.bottom + amount)
    val glowBrush = Brush.radialGradient(
        colors = listOf(
            Color.White.copy(alpha = alpha * 0.9f),
            blastColor.copy(alpha = alpha * 0.5f),
            Color.Transparent
        ),
        center = rect.center,
        radius = extraRect.width / 2f
    )
    drawRoundRect(
        brush = glowBrush,
        topLeft = extraRect.topLeft,
        size = extraRect.size,
        cornerRadius = CornerRadius(12.dp.toPx())
    )
}

private fun DrawScope.drawGrid(cellSize: Float, boardSize: Int, gridColor: Color) {
    for (i in 1 until boardSize) {
        val x = i * cellSize;
        val y = i * cellSize
        drawLine(gridColor.copy(alpha = 0.35f), Offset(x, 0f), Offset(x, size.height), 3.0f)
        drawLine(gridColor.copy(alpha = 0.35f), Offset(0f, y), Offset(size.width, y), 3.0f)
    }
}

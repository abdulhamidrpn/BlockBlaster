package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.rpn.blockblaster.core.designsystem.BlockColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ExplosionEffect(
    blastingCells: Set<Pair<Int, Int>>,
    boardOriginX:  Float,
    boardOriginY:  Float,
    cellSize:      Float,
    modifier:      Modifier = Modifier
) {
    if (blastingCells.isEmpty() || cellSize <= 0f) return

    data class Particle(
        val x: Float, val y: Float,
        val vx: Float, val vy: Float,
        val color: Color, val radius: Float
    )

    val particles = remember(blastingCells) {
        blastingCells.flatMap { (row, col) ->
            val cx = boardOriginX + col * cellSize + cellSize / 2f
            val cy = boardOriginY + row * cellSize + cellSize / 2f
            List(6) { i ->
                val angle = (i / 6f) * 2f * Math.PI.toFloat()
                val speed = Random.nextFloat() * 220f + 140f
                Particle(
                    x      = cx,
                    y      = cy,
                    vx     = cos(angle) * speed,
                    vy     = sin(angle) * speed,
                    color  = BlockColors[(row + col + i) % BlockColors.size],
                    radius = Random.nextFloat() * 5f + 3f
                )
            }
        }
    }

    // FIX: Use Animatable for a one-shot 0->1 animation.
    // infiniteTransition.animateFloat() requires InfiniteRepeatableSpec — passing tween()
    // directly causes "Argument type mismatch: actual type is TweenSpec<T>" compile error.
    val progress = remember { Animatable(0f) }
    LaunchedEffect(blastingCells) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 600, easing = LinearEasing)
        )
    }

    val p = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val alpha = (1f - p * 1.6f).coerceIn(0f, 1f)
            if (alpha > 0f) {
                drawCircle(
                    color  = particle.color.copy(alpha = alpha),
                    radius = particle.radius * (1f + p * 0.5f),
                    center = Offset(
                        particle.x + particle.vx * p * 0.6f,
                        particle.y + particle.vy * p * 0.6f
                    )
                )
            }
        }
    }
}

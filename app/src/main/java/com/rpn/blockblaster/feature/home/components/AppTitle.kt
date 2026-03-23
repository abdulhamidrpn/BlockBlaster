package com.rpn.blockblaster.feature.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

// ─────────────────────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────────────────────

private data class TitleParticle(
    val targetX:  Float,
    val targetY:  Float,
    val startX:   Float,
    val startY:   Float,
    val blastVx:  Float,
    val blastVy:  Float,
    val color:    Color,
    val cellPx:   Float          // each word uses its own cell size
)

// ─────────────────────────────────────────────────────────────────────────────
// 5×7 pixel font  (every letter used by BLOCK + BLASTER)
// ─────────────────────────────────────────────────────────────────────────────

private val PIXEL_FONT = mapOf(
    'B' to listOf(
        "11110",
        "10001",
        "10001",
        "11110",
        "10001",
        "10001",
        "11110"
    ),
    'L' to listOf(
        "10000",
        "10000",
        "10000",
        "10000",
        "10000",
        "10000",
        "11111"
    ),
    'O' to listOf(
        "01110",
        "10001",
        "10001",
        "10001",
        "10001",
        "10001",
        "01110"
    ),
    'C' to listOf(
        "01111",
        "10000",
        "10000",
        "10000",
        "10000",
        "10000",
        "01111"
    ),
    'K' to listOf(
        "10001",
        "10010",
        "10100",
        "11000",
        "10100",
        "10010",
        "10001"
    ),
    'A' to listOf(
        "01110",
        "10001",
        "10001",
        "11111",
        "10001",
        "10001",
        "10001"
    ),
    'S' to listOf(
        "01111",
        "10000",
        "10000",
        "01110",
        "00001",
        "00001",
        "11110"
    ),
    'T' to listOf(
        "11111",
        "00100",
        "00100",
        "00100",
        "00100",
        "00100",
        "00100"
    ),
    'E' to listOf(
        "11111",
        "10000",
        "10000",
        "11110",
        "10000",
        "10000",
        "11111"
    ),
    'R' to listOf(
        "11110",
        "10001",
        "10001",
        "11110",
        "10100",
        "10010",
        "10001"
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Palette  – vivid arcade colours matching the reference image
// ─────────────────────────────────────────────────────────────────────────────

private val ARCADE_PALETTE = listOf(
//    Color(0xFFF4A623),   // 0 orange
//    Color(0xFF4FC3F7),   // 1 sky-blue
    Color(0xFFFF4444),   // 2 red
//    Color(0xFFFFE030),   // 3 bright yellow
    Color(0xFFCE93D8),   // 4 lilac-purple
//    Color(0xFF4DD0E1),   // 5 cyan-teal
)

// Deterministic colour index per letter × row × col so every render is stable
private fun pickColor(letterIdx: Int, row: Int, col: Int): Color =
    ARCADE_PALETTE[(letterIdx * 3 + row + col * 2) % ARCADE_PALETTE.size]

// ─────────────────────────────────────────────────────────────────────────────
// AppTitle composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppTitle() {

    val density = LocalDensity.current

    // Cell size in px for each word
    val bigCell  = with(density) { 9.dp.toPx() }   // BLOCK  – large
    val smCell   = with(density) { 7.5.dp.toPx() }  // BLASTER – smaller
    val letterGap = with(density) { 5.dp.toPx() }
    val rowGap    = with(density) { 16.dp.toPx() }

    var canvasSizePx by remember { mutableStateOf(Size.Zero) }

    val particles: List<TitleParticle> = remember(canvasSizePx) {
        if (canvasSizePx == Size.Zero) return@remember emptyList()
        buildTitleParticles(
            canvasSizePx = canvasSizePx,
            bigCellPx    = bigCell,
            smCellPx     = smCell,
            letterGapPx  = letterGap,
            rowGapPx     = rowGap
        )
    }

    // ── Phase machine ─────────────────────────────────────────────────────────
    val phase = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            phase.animateTo(
                targetValue   = 1f,
                animationSpec = tween(
                    durationMillis = 1800,
                    easing         = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                )
            )
            kotlinx.coroutines.delay(900)
            phase.animateTo(
                targetValue   = 2f,
                animationSpec = tween(durationMillis = 700, easing = FastOutLinearInEasing)
            )
            kotlinx.coroutines.delay(300)
            phase.snapTo(0f)
            kotlinx.coroutines.delay(150)
        }
    }

    val p = phase.value

    val inf = rememberInfiniteTransition(label = "titleInf")

    val holdPulse by inf.animateFloat(
        initialValue  = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "holdPulse"
    )
    val tagAlpha by inf.animateFloat(
        initialValue  = 0.45f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "tagAlpha"
    )

    val assembledFrac = ((1f - kotlin.math.abs(p - 1f)) * 4f).coerceIn(0f, 1f)
    val canvasScale   = 1f + assembledFrac * (holdPulse - 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .graphicsLayer { scaleX = canvasScale; scaleY = canvasScale }
                .onSizeChanged { size ->
                    canvasSizePx = Size(size.width.toFloat(), size.height.toFloat())
                }
        ) {
            particles.forEach { particle ->
                val x: Float
                val y: Float
                val alpha: Float
                val cellSize: Float

                when {
                    p <= 1f -> {
                        val raw = p.coerceIn(0f, 1f)
                        val t   = 1f - (1f - raw).let { it * it * it }
                        x        = particle.startX + (particle.targetX - particle.startX) * t
                        y        = particle.startY + (particle.targetY - particle.startY) * t
                        alpha    = (raw * 3f).coerceIn(0f, 1f)
                        cellSize = particle.cellPx
                    }
                    else -> {
                        val t     = (p - 1f).coerceIn(0f, 1f)
                        val eased = t * t
                        val speed = this.size.width.coerceAtLeast(this.size.height)
                        x        = particle.targetX + particle.blastVx * eased * speed * 0.9f
                        y        = particle.targetY + particle.blastVy * eased * speed * 0.7f
                        alpha    = (1f - t * 1.5f).coerceIn(0f, 1f)
                        cellSize = particle.cellPx * (1f + eased * 0.6f)
                    }
                }

                if (alpha > 0.01f) {
                    val c  = particle.color
                    val r  = cellSize * 0.24f
                    val s  = cellSize * 0.86f  // face size

                    // ── Bottom-right shadow (deep for bold 3D look) ───────────
                    drawRoundRect(
                        color        = Color(
                            c.red   * 0.25f,
                            c.green * 0.25f,
                            c.blue  * 0.25f,
                            alpha * 0.70f
                        ),
                        topLeft      = Offset(x + cellSize * 0.18f, y + cellSize * 0.20f),
                        size         = Size(s, s),
                        cornerRadius = CornerRadius(r)
                    )

                    // ── Mid shadow (softer, gives thickness) ──────────────────
                    drawRoundRect(
                        color        = Color(
                            c.red   * 0.55f,
                            c.green * 0.55f,
                            c.blue  * 0.55f,
                            alpha * 0.55f
                        ),
                        topLeft      = Offset(x + cellSize * 0.09f, y + cellSize * 0.10f),
                        size         = Size(s, s),
                        cornerRadius = CornerRadius(r)
                    )

                    // ── Main face ────────────────────────────────────────────
                    drawRoundRect(
                        color        = c.copy(alpha = alpha),
                        topLeft      = Offset(x, y),
                        size         = Size(s, s),
                        cornerRadius = CornerRadius(r)
                    )

                    // ── Large top highlight (glossy dome effect) ─────────────
                    drawRoundRect(
                        color        = Color(
                            (c.red   * 1.55f).coerceAtMost(1f),
                            (c.green * 1.55f).coerceAtMost(1f),
                            (c.blue  * 1.55f).coerceAtMost(1f),
                            alpha * 0.75f
                        ),
                        topLeft      = Offset(x + cellSize * 0.06f, y + cellSize * 0.05f),
                        size         = Size(s * 0.55f, s * 0.30f),
                        cornerRadius = CornerRadius(r * 0.6f)
                    )

                    // ── Small inner glint (top-left corner dot) ───────────────
                    drawCircle(
                        color  = Color(1f, 1f, 1f, alpha * 0.55f),
                        radius = cellSize * 0.09f,
                        center = Offset(x + cellSize * 0.18f, y + cellSize * 0.16f)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text          = "Drop. Blast. Repeat.",
            fontSize      = 13.sp,
            fontWeight    = FontWeight.SemiBold,
            color         = MaterialTheme.colorScheme.onSurface.copy(alpha = tagAlpha),
            letterSpacing = 2.5.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Particle builder
// ─────────────────────────────────────────────────────────────────────────────

private fun buildTitleParticles(
    canvasSizePx: Size,
    bigCellPx:    Float,   // cell size for "BLOCK"
    smCellPx:     Float,   // cell size for "BLASTER"
    letterGapPx:  Float,
    rowGapPx:     Float
): List<TitleParticle> {

    val rng = java.util.Random(1337L)

    val word1 = "BLOCK"
    val word2 = "BLASTER"

    // Each word has its own letter stride
    val bigStride = 5f * bigCellPx + letterGapPx
    val smStride  = 5f * smCellPx  + letterGapPx

    val h1 = 7f * bigCellPx
    val h2 = 7f * smCellPx

    fun wordWidth(word: String, stride: Float) = word.length * stride - letterGapPx

    val w1   = wordWidth(word1, bigStride)
    val w2   = wordWidth(word2, smStride)
    val totalH = h1 + rowGapPx + h2
    val ox1  = (canvasSizePx.width - w1) / 2f
    val ox2  = (canvasSizePx.width - w2) / 2f
    val oy1  = (canvasSizePx.height - totalH) / 2f
    val oy2  = oy1 + h1 + rowGapPx
    val cx   = canvasSizePx.width  / 2f
    val cy   = canvasSizePx.height / 2f

    val result = mutableListOf<TitleParticle>()

    fun addLetter(
        ch:        Char,
        originX:   Float,
        originY:   Float,
        cellPx:    Float,
        letterIdx: Int
    ) {
        val grid = PIXEL_FONT[ch] ?: return
        grid.forEachIndexed { row, rowStr ->
            rowStr.forEachIndexed { col, cell ->
                if (cell == '1') {
                    val tx = originX + col * cellPx
                    val ty = originY + row * cellPx

                    // Random start on any screen edge
                    val side = rng.nextInt(4)
                    val sx = when (side) {
                        0    -> rng.nextFloat() * canvasSizePx.width
                        1    -> canvasSizePx.width + cellPx * 4f
                        2    -> rng.nextFloat() * canvasSizePx.width
                        else -> -cellPx * 4f
                    }
                    val sy = when (side) {
                        0    -> -cellPx * 4f
                        1    -> rng.nextFloat() * canvasSizePx.height
                        2    -> canvasSizePx.height + cellPx * 4f
                        else -> rng.nextFloat() * canvasSizePx.height
                    }

                    // Blast away from text centre
                    val dx  = (tx - cx).let { if (it == 0f) 0.01f else it }
                    val dy  = (ty - cy).let { if (it == 0f) 0.01f else it }
                    val len = kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    val bvx = dx / len + (rng.nextFloat() - 0.5f) * 0.6f
                    val bvy = dy / len + (rng.nextFloat() - 0.5f) * 0.6f

                    result += TitleParticle(
                        targetX = tx,  targetY = ty,
                        startX  = sx,  startY  = sy,
                        blastVx = bvx, blastVy = bvy,
                        color   = pickColor(letterIdx, row, col),
                        cellPx  = cellPx
                    )
                }
            }
        }
    }

    word1.forEachIndexed { i, ch ->
        addLetter(ch, ox1 + i * bigStride, oy1, bigCellPx, i)
    }
    word2.forEachIndexed { i, ch ->
        addLetter(ch, ox2 + i * smStride, oy2, smCellPx, word1.length + i)
    }

    return result
}
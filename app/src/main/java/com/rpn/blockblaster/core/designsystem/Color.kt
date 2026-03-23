package com.rpn.blockblaster.core.designsystem

import androidx.compose.ui.graphics.Color

// ── Dark theme palette ──────────────────────────────────────────────────────
val BackgroundDark        = Color(0xFF1A1A2E)
val BackgroundSecondary   = Color(0xFF16213E)
val BackgroundElevated    = Color(0xFF0F3460)
val TextPrimary           = Color(0xFFE0E0E0)
val TextSecondary         = Color(0xFF9E9E9E)
val BoardBackground       = Color(0xFF12122A)
val GridLine              = Color(0xFF38385A)
val AccentRed             = Color(0xFFE94560)
val GoldColor             = Color(0xFFFFD700)
val GoldDim               = Color(0xFFB8960C)

// ── Light theme palette ─────────────────────────────────────────────────────
val BackgroundLight       = Color(0xFFF5F5F0)
val BackgroundSecLight    = Color(0xFFE8E8E0)
val BackgroundElevLight   = Color(0xFFFFFFFF)
val TextPrimaryLight      = Color(0xFF1A1A2E)
val TextSecondaryLight    = Color(0xFF555566)
val BoardBackgroundLight  = Color(0xFFDDDDD8)
val GridLineLight         = Color(0xFF999999)
val AccentRedLight        = Color(0xFFCC3355)

// ── Block colours (10 vivid, all work on dark + light boards) ────────────────
val BlockCoral    = Color(0xFFFF3366)
val BlockTeal     = Color(0xFF00E5FF)
val BlockGold     = Color(0xFFFFD700)
val BlockLavender = Color(0xFFB388FF)
val BlockMint     = Color(0xFF1DE9B6)
val BlockPeach    = Color(0xFFFF6D00)
val BlockSky      = Color(0xFF40C4FF)
val BlockRose     = Color(0xFFFF4081)
val BlockLime     = Color(0xFFAEEA00)
val BlockSteel    = Color(0xFF90A4AE)

val BlockColors = listOf(
    BlockCoral, BlockTeal, BlockGold, BlockLavender, BlockMint,
    BlockPeach, BlockSky, BlockRose, BlockLime, BlockSteel
)

fun Color.highlight(): Color = Color(
    red   = (red   * 1.35f).coerceAtMost(1f),
    green = (green * 1.35f).coerceAtMost(1f),
    blue  = (blue  * 1.35f).coerceAtMost(1f),
    alpha = alpha
)

fun Color.shadow(): Color = Color(
    red   = red   * 0.55f,
    green = green * 0.55f,
    blue  = blue  * 0.55f,
    alpha = alpha
)

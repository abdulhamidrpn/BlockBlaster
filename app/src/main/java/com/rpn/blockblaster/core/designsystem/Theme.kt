package com.rpn.blockblaster.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = AccentRed,
    onPrimary        = Color.White,
    secondary        = GoldColor,
    onSecondary      = Color.Black,
    background       = BackgroundDark,
    onBackground     = TextPrimary,
    surface          = BackgroundSecondary,
    onSurface        = TextPrimary,
    surfaceVariant   = BackgroundElevated,
    onSurfaceVariant = TextSecondary,
    error            = Color(0xFFCF6679),
    onError          = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary          = AccentRedLight,
    onPrimary        = Color.White,
    secondary        = GoldDim,
    onSecondary      = Color.White,
    background       = BackgroundLight,
    onBackground     = TextPrimaryLight,
    surface          = BackgroundSecLight,
    onSurface        = TextPrimaryLight,
    surfaceVariant   = BackgroundElevLight,
    onSurfaceVariant = TextSecondaryLight,
    error            = Color(0xFFB00020),
    onError          = Color.White
)

@Composable
fun BlockBlasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GameTypography,
        content     = content
    )
}

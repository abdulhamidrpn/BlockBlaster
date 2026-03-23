package com.rpn.blockblaster.feature.game.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.AccentRed

@Composable
fun PauseOverlay(
    visible:    Boolean,
    onResume:   () -> Unit,
    onRestart:  () -> Unit,
    onSettings: () -> Unit,
    onHome:     () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn() + slideInVertically { it },
        exit    = fadeOut() + slideOutVertically { it }
    ) {
        Box(
            modifier         = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier  = Modifier.fillMaxWidth(0.82f),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("PAUSED", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface, letterSpacing = 4.sp)
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                    Button3D("▶  RESUME",   onResume,   Modifier.fillMaxWidth(), AccentRed)
                    Button3D("↺  RESTART",  onRestart,  Modifier.fillMaxWidth(), Color(0xFF0F3460))
                    Button3D("⚙  SETTINGS", onSettings, Modifier.fillMaxWidth(), Color(0xFF2D2D50))
                    Button3D("⌂  HOME",     onHome,     Modifier.fillMaxWidth(), Color(0xFF333355))
                }
            }
        }
    }
}

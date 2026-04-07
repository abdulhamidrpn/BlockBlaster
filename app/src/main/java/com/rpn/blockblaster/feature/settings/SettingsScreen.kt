package com.rpn.blockblaster.feature.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.rpn.blockblaster.core.designsystem.AccentRed
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import android.app.Activity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.rpn.blockblaster.core.play.PlayServicesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val playServicesManager: PlayServicesManager = koinInject()
    val playGamesManager: com.rpn.blockblaster.core.play.PlayGamesManager = koinInject()
    val profile by playGamesManager.profileState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(vm) {
        vm.events.collectLatest { event ->
            when (event) {
                is SettingsUiEvent.NavigateBack -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SETTINGS", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!state.isLoading) {
                val s = state.settings

                SettingsSectionHeader("🔊  Audio")
                SettingsToggleRow("Sound Effects",    s.soundEnabled)    { vm.onIntent(SettingsIntent.Save(s.copy(soundEnabled = it))) }
                SettingsToggleRow("Background Music", s.bgmEnabled)      { vm.onIntent(SettingsIntent.Save(s.copy(bgmEnabled = it))) }
                SettingsToggleRow("Vibration",        s.vibrationEnabled){ vm.onIntent(SettingsIntent.Save(s.copy(vibrationEnabled = it))) }

                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("🎨  Display")
                SettingsToggleRow("Dark Theme",       s.isDarkTheme)     { vm.onIntent(SettingsIntent.Save(s.copy(isDarkTheme = it))) }
                SettingsToggleRow("Show Grid Lines",  s.showGridLines)   { vm.onIntent(SettingsIntent.Save(s.copy(showGridLines = it))) }

                Spacer(Modifier.height(8.dp))
                SettingsSectionHeader("⚡  Animation Speed")
                AnimSpeedRow(currentMs = s.animSpeedMs) { ms ->
                    vm.onIntent(SettingsIntent.Save(s.copy(animSpeedMs = ms)))
                }

                Spacer(Modifier.height(16.dp))
                SettingsSectionHeader("🎮  Google Play Games")
                if (profile != null) {
                    SettingsClickableRow("Connected as ${profile!!.displayName}") {
                        // Action could be to show achievements/leaderboards or sign out.
                        val activity = context as? Activity
                        activity?.let { playGamesManager.showLeaderboard(it) }
                    }
                } else {
                    SettingsClickableRow("Connect Account") {
                        val activity = context as? Activity
                        activity?.let { playGamesManager.requestManualSignIn(it) }
                    }
                }

                Spacer(Modifier.height(16.dp))
                SettingsSectionHeader("⭐️  Support Us")
                SettingsClickableRow("Rate This App") {
                    coroutineScope.launch {
                        val activity = context as? Activity
                        if (activity != null) {
                            val success = playServicesManager.requestInAppReview(activity)
                            if (!success) playServicesManager.openPlayStoreForReview()
                        }
                    }
                }
                SettingsClickableRow("More Apps") {
                    playServicesManager.openMoreApps("RPN Play Inc.") // replace "RPN" if needed
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                Spacer(Modifier.height(8.dp))
                Text("Version 1.0.3", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.4f))
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        color = AccentRed, letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f))
        AnimatedToggle(checked = checked, onToggle = onToggle)
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun AnimatedToggle(checked: Boolean, onToggle: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(
        if (checked) AccentRed else MaterialTheme.colorScheme.onSurface.copy(0.2f),
        label = "trackColor"
    )
    val thumbOffset by animateDpAsState(
        if (checked) 20.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "thumbOffset"
    )
    Box(
        modifier = Modifier
            .width(48.dp).height(26.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(trackColor)
            .clickable { onToggle(!checked) }
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun AnimSpeedRow(currentMs: Int, onSelect: (Int) -> Unit) {
    val speeds = listOf("Slow" to 500, "Normal" to 300, "Fast" to 150)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        speeds.forEach { (label, ms) ->
            val selected = currentMs == ms
            Box(
                modifier = Modifier
                    .background(
                        if (selected) AccentRed else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (selected) AccentRed else MaterialTheme.colorScheme.onSurface.copy(0.2f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(ms) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun SettingsClickableRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f))
        Icon(
            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, // Will flip or just use a generic icon
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.graphicsLayer(rotationZ = 180f).size(18.dp) // Point right
        )
    }
    Spacer(Modifier.height(4.dp))
}

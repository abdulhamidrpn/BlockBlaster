package com.rpn.blockblaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rpn.blockblaster.core.designsystem.BlockBlasterTheme
import com.rpn.blockblaster.core.navigation.AppNavGraph

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rpn.blockblaster.data.local.datastore.SettingsDataStore
import com.rpn.blockblaster.domain.model.AppSettings
import com.rpn.blockblaster.service.AdManager
import com.google.android.gms.ads.MobileAds
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val adManager: AdManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        
        // Initialize AdMob
        MobileAds.initialize(this) {}
        adManager.loadReviveAd()
        
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val settingsDataStore = remember { SettingsDataStore(context) }
            val settings by settingsDataStore.getSettings().collectAsState(initial = AppSettings())

            BlockBlasterTheme(darkTheme = settings.isDarkTheme) {
                AppNavGraph()
            }
        }
    }
}

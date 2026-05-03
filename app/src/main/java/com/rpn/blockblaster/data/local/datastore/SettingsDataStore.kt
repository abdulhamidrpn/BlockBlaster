package com.rpn.blockblaster.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rpn.blockblaster.domain.engine.Difficulty
import com.rpn.blockblaster.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val KEY_SOUND     = booleanPreferencesKey("sound")
        val KEY_BGM       = booleanPreferencesKey("bgm")
        val KEY_VIBRATION = booleanPreferencesKey("vibration")
        val KEY_DARK      = booleanPreferencesKey("dark_theme")
        val KEY_GRID      = booleanPreferencesKey("show_grid")
        val KEY_ANIM_MS   = intPreferencesKey("anim_speed_ms")
        val KEY_GAMES_PLAYED = intPreferencesKey("games_played")
        val KEY_DIFFICULTY = stringPreferencesKey("difficulty")
    }

    fun getSettings(): Flow<AppSettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AppSettings(
                soundEnabled     = prefs[KEY_SOUND]     ?: true,
                bgmEnabled       = prefs[KEY_BGM]       ?: true,
                vibrationEnabled = prefs[KEY_VIBRATION] ?: true,
                isDarkTheme      = prefs[KEY_DARK]      ?: true,
                showGridLines    = prefs[KEY_GRID]      ?: true,
                animSpeedMs      = prefs[KEY_ANIM_MS]   ?: 300,
                gamesPlayed      = prefs[KEY_GAMES_PLAYED] ?: 0,
                difficulty       = Difficulty.valueOf(prefs[KEY_DIFFICULTY] ?: Difficulty.MEDIUM.name)
            )
        }

    suspend fun save(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND]     = settings.soundEnabled
            prefs[KEY_BGM]       = settings.bgmEnabled
            prefs[KEY_VIBRATION] = settings.vibrationEnabled
            prefs[KEY_DARK]      = settings.isDarkTheme
            prefs[KEY_GRID]      = settings.showGridLines
            prefs[KEY_ANIM_MS]   = settings.animSpeedMs
            prefs[KEY_GAMES_PLAYED] = settings.gamesPlayed
            prefs[KEY_DIFFICULTY] = settings.difficulty.name
        }
    }
}

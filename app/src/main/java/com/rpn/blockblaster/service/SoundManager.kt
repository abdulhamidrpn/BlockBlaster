package com.rpn.blockblaster.service

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.MediaPlayer
import com.rpn.blockblaster.R

enum class SoundType {
    BLOCK_PLACE, BLAST, CROSS_BLAST, PERFECT_CLEAR,
    COMBO, GAME_OVER, REVIVE, BUTTON_CLICK, INVALID
}

class SoundManager(private val context: Context) {

    private val pool: SoundPool by lazy {
        SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            ).build()
    }

    private val soundMap = mutableMapOf<SoundType, Int>()
    var enabled = true
    
    var bgmEnabled = true
        set(value) {
            field = value
            if (value) resumeBgm() else pauseBgm()
        }

    private var bgmPlayer: MediaPlayer? = null

    init { loadAll() }

    private fun loadAll() {
        // Attempt to load BGM if the raw file exists, otherwise catch gracefully
        try {
            val bgmResId = context.resources.getIdentifier("bgm", "raw", context.packageName)
            if (bgmResId != 0) {
                bgmPlayer = MediaPlayer.create(context, bgmResId)
                bgmPlayer?.isLooping = true
            }
        } catch (_: Exception) {}

        val rawMap = mapOf(
            SoundType.BLOCK_PLACE   to R.raw.block_place,
            SoundType.BLAST         to R.raw.blast,
            SoundType.CROSS_BLAST   to R.raw.cross_blast,
            SoundType.PERFECT_CLEAR to R.raw.perfect_clear,
            SoundType.COMBO         to R.raw.combo,
            SoundType.GAME_OVER     to R.raw.game_over,
            SoundType.REVIVE        to R.raw.revive,
            SoundType.BUTTON_CLICK  to R.raw.button_click,
            SoundType.INVALID       to R.raw.invalid
        )
        rawMap.forEach { (type, resId) ->
            try {
                val id = pool.load(context, resId, 1)
                if (id != 0) soundMap[type] = id
            } catch (_: Exception) { }
        }
    }

    fun play(type: SoundType, pitch: Float = 1f) {
        if (!enabled) return
        soundMap[type]?.let { id ->
            pool.play(id, 0.8f, 0.8f, 0, 0, pitch.coerceIn(0.5f, 2f))
        }
    }

    private fun resumeBgm() {
        if (bgmEnabled && bgmPlayer?.isPlaying == false) {
            bgmPlayer?.start()
        }
    }

    private fun pauseBgm() {
        if (bgmPlayer?.isPlaying == true) {
            bgmPlayer?.pause()
        }
    }

    // Call when app goes to background
    fun onPause() = pauseBgm()
    
    // Call when app returns to foreground
    fun onResume() = resumeBgm()

    fun release() {
        pool.release()
        bgmPlayer?.release()
        bgmPlayer = null
    }
}

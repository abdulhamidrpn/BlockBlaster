package com.rpn.blockblaster.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationManager(context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    var enabled = true

    fun vibrateLight() {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, 80))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(30)
        }
    }

    fun vibrateHeavy() {
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0L, 50L, 30L, 100L), intArrayOf(0, 180, 0, 255), -1)
            )
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(longArrayOf(0L, 50L, 30L, 100L), -1)
        }
    }
}

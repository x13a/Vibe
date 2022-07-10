package me.lucky.vibe

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import java.lang.NumberFormatException

class Vibrator(ctx: Context) {
    private val prefs = Preferences(ctx)
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        ctx.getSystemService(VibratorManager::class.java)?.defaultVibrator
    else
        ctx.getSystemService(Vibrator::class.java)

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(vibePattern(), -1))
        } else {
            @Suppress("deprecation")
            vibrator?.vibrate(vibePattern(), -1)
        }
    }

    private fun vibePattern(): LongArray? {
        val toLongArray = { str: String ->
            try {
                str.split(Preferences.VIBE_PATTERN_DELIMITER).map { it.toLong() }.toLongArray()
            } catch (exc: NumberFormatException) { null }
        }
        return toLongArray(prefs.vibePattern) ?: toLongArray(Preferences.DEFAULT_VIBE_PATTERN)
    }
}
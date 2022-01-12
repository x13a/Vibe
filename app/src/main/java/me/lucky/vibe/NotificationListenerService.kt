package me.lucky.vibe

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi

class NotificationListenerService : NotificationListenerService() {
    companion object {
        private const val DIALER_SUFFIX = ".dialer"
        private val VIBE_PATTERN = longArrayOf(50, 100, 50, 100)
    }

    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private var vibrationEffect: VibrationEffect? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?)?.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationEffect = VibrationEffect.createWaveform(VIBE_PATTERN, -1)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (audioManager?.mode != AudioManager.MODE_IN_CALL ||
            !sbn.isOngoing ||
            !sbn.packageName.endsWith(DIALER_SUFFIX) ||
            !sbn.notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)) return
        vibrate()
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(vibrationEffect)
        } else {
            vibrator?.vibrate(VIBE_PATTERN, -1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val packages = packageManager
            .getInstalledPackages(0)
            .map { it.packageName }
            .toMutableSet()
        packages.addAll(packageManager
            .queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                0,
            )
            .map{ it.activityInfo.packageName })
        migrateNotificationFilter(0, packages.filterNot { it.endsWith(DIALER_SUFFIX) })
    }
}

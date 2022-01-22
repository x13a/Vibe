package me.lucky.vibe

import android.app.Notification
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager
import android.util.Log
import androidx.annotation.RequiresApi

class NotificationListenerService : NotificationListenerService() {
    companion object {
        private val TAG = NotificationListenerService::class.java.simpleName
        private const val DIALER_SUFFIX = ".dialer"
        private const val MAX_DELAY = 2000
        private val VIBE_PATTERN = longArrayOf(50, 100, 50, 100)
    }

    private lateinit var prefs: Preferences
    private var telecomManager: TelecomManager? = null
    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null
    @RequiresApi(Build.VERSION_CODES.O)
    private var vibrationEffect: VibrationEffect? = null

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        prefs = Preferences(this)
        telecomManager = getSystemService(TelecomManager::class.java)
        audioManager = getSystemService(AudioManager::class.java)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            getSystemService(Vibrator::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrationEffect = VibrationEffect.createWaveform(VIBE_PATTERN, -1)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        if (audioManager?.mode != AudioManager.MODE_IN_CALL
            || !sbn.isOngoing
            || (prefs.isFilterPackageNames
                && !sbn.packageName.endsWith(DIALER_SUFFIX)
                && sbn.packageName != telecomManager?.defaultDialerPackage)
            || !sbn.notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)
            || sbn.notification.extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN)
            || sbn.notification.`when` < System.currentTimeMillis() - MAX_DELAY) return
        vibrate()
    }

    private fun vibrate() {
        Log.d(TAG, "vibrate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(vibrationEffect)
        } else {
            @Suppress("deprecation")
            vibrator?.vibrate(VIBE_PATTERN, -1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !prefs.isFilterPackageNames) return
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
        migrateNotificationFilter(
            0,
            packages.filterNot {
                it.endsWith(DIALER_SUFFIX) ||
                it == telecomManager?.defaultDialerPackage
            },
        )
    }
}

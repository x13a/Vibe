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
import java.lang.NumberFormatException

class NotificationListenerService : NotificationListenerService() {
    companion object {
        private val TAG = NotificationListenerService::class.java.simpleName
        private const val DIALER_SUFFIX = ".dialer"
        private const val MAX_DELAY = 2000
    }

    private lateinit var prefs: Preferences
    private var telecomManager: TelecomManager? = null
    private var audioManager: AudioManager? = null
    private var vibrator: Vibrator? = null

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
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null
            || (audioManager?.mode != AudioManager.MODE_IN_CALL
                && audioManager?.mode != AudioManager.MODE_IN_COMMUNICATION)
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
        var res = toLongArray(prefs.vibePattern)
        if (res == null) res = toLongArray(Preferences.DEFAULT_VIBE_PATTERN)
        return res
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !prefs.isFilterPackageNames) return
        migrate()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun migrate() {
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

package me.lucky.vibe

import android.app.Notification
import android.media.AudioManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telecom.TelecomManager

class NotificationListenerService : NotificationListenerService() {
    companion object {
        private const val DIALER_SUFFIX = ".dialer"
        private const val MAX_DELAY = 2000L
    }

    private lateinit var prefs: Preferences
    private lateinit var vibrator: Vibrator
    private var telecomManager: TelecomManager? = null
    private var audioManager: AudioManager? = null
    private var key: String? = null

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        prefs = Preferences(this)
        vibrator = Vibrator(this)
        telecomManager = getSystemService(TelecomManager::class.java)
        audioManager = getSystemService(AudioManager::class.java)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val isVibeAtStart = prefs.isVibeAtStart
        if (sbn == null
            || !(isVibeAtStart || prefs.isVibeAtEnd)
            || (audioManager?.mode != AudioManager.MODE_IN_CALL
                && audioManager?.mode != AudioManager.MODE_IN_COMMUNICATION)
            || !sbn.isOngoing
            || (prefs.isFilterPackageNames
                && !sbn.packageName.endsWith(DIALER_SUFFIX)
                && sbn.packageName != telecomManager?.defaultDialerPackage)
            || !sbn.notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER)
            || sbn.notification.extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN)
            || sbn.notification.`when` < System.currentTimeMillis() - MAX_DELAY) return
        key = sbn.key
        if (!isVibeAtStart) return
        vibrator.vibrate()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null || !prefs.isVibeAtEnd || key != sbn.key) return
        key = null
        vibrator.vibrate()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            migrateNotificationFilter(0, null)
    }
}
package me.lucky.vibe

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        private const val FILTER_PACKAGE_NAMES = "filter_package_names"
        private const val VIBE_PATTERN = "vibe_pattern"

        const val VIBE_PATTERN_DELIMITER = ","
        val DEFAULT_VIBE_PATTERN = arrayOf("0", "200")
            .joinToString(VIBE_PATTERN_DELIMITER)
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isFilterPackageNames: Boolean
        get() = prefs.getBoolean(FILTER_PACKAGE_NAMES, true)
        set(value) = prefs.edit { putBoolean(FILTER_PACKAGE_NAMES, value) }

    var vibePattern: String
        get() = prefs.getString(VIBE_PATTERN, DEFAULT_VIBE_PATTERN) ?: DEFAULT_VIBE_PATTERN
        set(value) = prefs.edit { putString(VIBE_PATTERN, value) }
}

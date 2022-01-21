package me.lucky.vibe

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class Preferences(ctx: Context) {
    companion object {
        private const val FILTER_PACKAGE_NAMES = "filter_package_names"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var isFilterPackageNames: Boolean
        get() = prefs.getBoolean(FILTER_PACKAGE_NAMES, true)
        set(value) = prefs.edit { putBoolean(FILTER_PACKAGE_NAMES, value) }
}

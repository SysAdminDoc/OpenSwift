package com.openswift.keyboard.data

import android.content.Context
import androidx.preference.PreferenceManager

class Settings(ctx: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    var theme: String
        get() = prefs.getString("theme", "amoled")!!
        set(v) { prefs.edit().putString("theme", v).apply() }

    var layout: String
        get() = prefs.getString("layout", "qwerty")!!
        set(v) { prefs.edit().putString("layout", v).apply() }

    var glideEnabled: Boolean
        get() = prefs.getBoolean("glide", true)
        set(v) { prefs.edit().putBoolean("glide", v).apply() }

    var autoCorrect: Boolean
        get() = prefs.getBoolean("autocorrect", true)
        set(v) { prefs.edit().putBoolean("autocorrect", v).apply() }

    var autoCapitalize: Boolean
        get() = prefs.getBoolean("autocap", true)
        set(v) { prefs.edit().putBoolean("autocap", v).apply() }

    var numberRow: Boolean
        get() = prefs.getBoolean("numrow", false)
        set(v) { prefs.edit().putBoolean("numrow", v).apply() }

    var hapticFeedback: Boolean
        get() = prefs.getBoolean("haptic", true)
        set(v) { prefs.edit().putBoolean("haptic", v).apply() }

    var soundFeedback: Boolean
        get() = prefs.getBoolean("sound", false)
        set(v) { prefs.edit().putBoolean("sound", v).apply() }

    var keyHeightDp: Int
        get() = prefs.getInt("keyHeight", 56)
        set(v) { prefs.edit().putInt("keyHeight", v).apply() }
}

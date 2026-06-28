package com.openswift.keyboard.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class Settings(ctx: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        ctx,
        "openswift_prefs",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var theme: String
        get() = prefs.getString("theme", "amoled")!!
        set(v) { prefs.edit().putString("theme", v).apply() }

    var layout: String
        get() = prefs.getString("layout", "qwerty")!!
        set(v) { prefs.edit().putString("layout", v).apply() }

    var language: String
        get() = KeyboardLanguages.byCode(prefs.getString("language", KeyboardLanguages.English.code)).code
        set(v) {
            val language = KeyboardLanguages.byCode(v)
            val current = prefs.getString("language", KeyboardLanguages.English.code)
            val editor = prefs.edit().putString("language", language.code)
            if (current != language.code) {
                editor.putString("layout", language.layoutId)
            }
            editor.apply()
        }

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
        get() = prefs.getBoolean("numrow", true)
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

    var powerSaveMode: Boolean
        get() = prefs.getBoolean("powersave", false)
        set(v) { prefs.edit().putBoolean("powersave", v).apply() }

    var clipboardEnabled: Boolean
        get() = prefs.getBoolean("clipboard", false)
        set(v) { prefs.edit().putBoolean("clipboard", v).apply() }

    var perAppTint: Boolean
        get() = prefs.getBoolean("perapp_tint", false)
        set(v) { prefs.edit().putBoolean("perapp_tint", v).apply() }

    var incognitoMode: Boolean
        get() = prefs.getBoolean("incognito", false)
        set(v) { prefs.edit().putBoolean("incognito", v).apply() }

    var reducedMotion: Boolean
        get() = prefs.getBoolean("reduced_motion", false)
        set(v) { prefs.edit().putBoolean("reduced_motion", v).apply() }
}

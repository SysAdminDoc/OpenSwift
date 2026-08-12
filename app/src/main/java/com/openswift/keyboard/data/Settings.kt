package com.openswift.keyboard.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

internal interface SettingsStore {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun putStrings(values: Map<String, String>)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
}

private class SharedPreferencesSettingsStore(private val prefs: SharedPreferences) : SettingsStore {
    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun putStrings(values: Map<String, String>) {
        val editor = prefs.edit()
        values.forEach { (key, value) -> editor.putString(key, value) }
        editor.apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int = prefs.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}

internal class MutableMapSettingsStore(
    private val values: MutableMap<String, Any> = mutableMapOf()
) : SettingsStore {
    override fun getString(key: String, defaultValue: String): String =
        values[key] as? String ?: defaultValue

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun putStrings(values: Map<String, String>) {
        this.values.putAll(values)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        values[key] as? Boolean ?: defaultValue

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun getInt(key: String, defaultValue: Int): Int = values[key] as? Int ?: defaultValue

    override fun putInt(key: String, value: Int) {
        values[key] = value
    }
}

class Settings internal constructor(private val store: SettingsStore) {

    constructor(ctx: Context) : this(SharedPreferencesSettingsStore(encryptedPreferences(ctx)))

    var theme: String
        get() = store.getString("theme", "amoled")
        set(value) = store.putString("theme", value)

    var layout: String
        get() = store.getString("layout", "qwerty")
        set(value) = store.putString("layout", value)

    var language: String
        get() = KeyboardLanguages.byCode(
            store.getString("language", KeyboardLanguages.English.code)
        ).code
        set(value) {
            val language = KeyboardLanguages.byCode(value)
            val current = store.getString("language", KeyboardLanguages.English.code)
            val updates = mutableMapOf("language" to language.code)
            if (current != language.code) updates["layout"] = language.layoutId
            store.putStrings(updates)
        }

    var glideEnabled: Boolean
        get() = store.getBoolean("glide", true)
        set(value) = store.putBoolean("glide", value)

    var autoCorrect: Boolean
        get() = store.getBoolean("autocorrect", true)
        set(value) = store.putBoolean("autocorrect", value)

    var languageDetection: Boolean
        get() = store.getBoolean("language_detection", true)
        set(value) = store.putBoolean("language_detection", value)

    var autoCapitalize: Boolean
        get() = store.getBoolean("autocap", true)
        set(value) = store.putBoolean("autocap", value)

    var numberRow: Boolean
        get() = store.getBoolean("numrow", true)
        set(value) = store.putBoolean("numrow", value)

    var hapticFeedback: Boolean
        get() = store.getBoolean("haptic", true)
        set(value) = store.putBoolean("haptic", value)

    var soundFeedback: Boolean
        get() = store.getBoolean("sound", false)
        set(value) = store.putBoolean("sound", value)

    var keyHeightDp: Int
        get() = store.getInt("keyHeight", 56)
        set(value) = store.putInt("keyHeight", value)

    var powerSaveMode: Boolean
        get() = store.getBoolean("powersave", false)
        set(value) = store.putBoolean("powersave", value)

    var clipboardEnabled: Boolean
        get() = store.getBoolean("clipboard", false)
        set(value) = store.putBoolean("clipboard", value)

    var perAppTint: Boolean
        get() = store.getBoolean("perapp_tint", false)
        set(value) = store.putBoolean("perapp_tint", value)

    var incognitoMode: Boolean
        get() = store.getBoolean("incognito", false)
        set(value) = store.putBoolean("incognito", value)

    var reducedMotion: Boolean
        get() = store.getBoolean("reduced_motion", false)
        set(value) = store.putBoolean("reduced_motion", value)

    companion object {
        private fun encryptedPreferences(ctx: Context): SharedPreferences =
            EncryptedSharedPreferences.create(
                ctx,
                "openswift_prefs",
                MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
    }
}

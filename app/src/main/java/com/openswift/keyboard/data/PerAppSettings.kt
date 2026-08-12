package com.openswift.keyboard.data

import android.content.Context

/**
 * Per-app settings: disable glide in games, adjust key height per app.
 * Integrates with ActivityManager to detect foreground app.
 */
class PerAppSettings(ctx: Context) {

    private val prefs = SecurePreferences.open(ctx, TypedDataStores.PER_APP)

    data class AppConfig(
        val packageName: String,
        val glideDisabled: Boolean = false,
        val keyHeightOverride: Int? = null
    )

    fun setAppConfig(packageName: String, glideDisabled: Boolean = false, keyHeight: Int? = null) {
        prefs.edit()
            .putBoolean("glide_off_$packageName", glideDisabled)
            .putInt("height_$packageName", keyHeight ?: -1)
            .apply()
    }

    fun getAppConfig(packageName: String): AppConfig {
        val glideOff = prefs.getBoolean("glide_off_$packageName", false)
        val height = prefs.getInt("height_$packageName", -1)
        return AppConfig(packageName, glideOff, if (height < 0) null else height)
    }

    fun removeAppConfig(packageName: String) {
        prefs.edit()
            .remove("glide_off_$packageName")
            .remove("height_$packageName")
            .apply()
    }

    fun getAllAppConfigs(): List<AppConfig> {
        return prefs.all.keys
            .filter { it.startsWith("glide_off_") }
            .map { k -> k.removePrefix("glide_off_") }
            .map { pkg -> getAppConfig(pkg) }
    }
}

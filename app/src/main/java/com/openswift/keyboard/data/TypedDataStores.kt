package com.openswift.keyboard.data

import android.content.Context
import androidx.preference.PreferenceManager

/** Canonical names and reset behavior for stores containing user-entered or derived data. */
object TypedDataStores {
    const val CLIPBOARD_HISTORY = "clipboard_history"
    const val SNIPPETS = "snippets"
    const val ANALYTICS = "analytics"
    const val PER_APP = "per_app"
    const val CUSTOM_THEMES = "custom_themes"
    const val CUSTOM_LAYOUTS = "custom_layouts"
    const val EMOJI_PICKER = "emoji_picker"
    const val CUSTOM_THEME_PREFIX = "custom_"

    fun userDictionary(languageCode: String): String =
        if (languageCode == KeyboardLanguages.English.code) "user_dict" else "user_dict_$languageCode"

    fun clearAll(context: Context) {
        val standardStores = listOf(
            CLIPBOARD_HISTORY,
            SNIPPETS,
            ANALYTICS,
            PER_APP,
            EMOJI_PICKER,
            CUSTOM_LAYOUTS,
        ) + KeyboardLanguages.all.map { userDictionary(it.code) }
        standardStores.distinct().forEach { SecurePreferences.clear(context, it) }

        val legacyThemes = PreferenceManager.getDefaultSharedPreferences(context)
        SecurePreferences.clear(
            context = context,
            storeName = CUSTOM_THEMES,
            legacyPreferences = legacyThemes,
            migrateKey = { it.startsWith(CUSTOM_THEME_PREFIX) }
        )
    }
}

package com.openswift.keyboard.theme

import android.content.Context
import androidx.preference.PreferenceManager
import com.openswift.keyboard.data.SecurePreferences
import com.openswift.keyboard.data.TypedDataStores

/** Per-user custom theme editor: allows color customization of any theme. */
class ThemeEditor(ctx: Context) {

    private val legacyPrefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    private val prefs = SecurePreferences.open(
        context = ctx,
        storeName = TypedDataStores.CUSTOM_THEMES,
        legacyPreferences = legacyPrefs,
        migrateKey = { it.startsWith(TypedDataStores.CUSTOM_THEME_PREFIX) }
    )

    data class CustomTheme(
        val id: String,
        val name: String,
        val background: Int,
        val keyBackground: Int,
        val keyModifierBackground: Int,
        val keyText: Int,
        val keyAccent: Int,
        val suggestionBg: Int,
        val suggestionText: Int,
        val gestureTrail: Int
    )

    fun createCustom(baseTheme: KbTheme, edits: Map<String, Int>): CustomTheme {
        val id = TypedDataStores.CUSTOM_THEME_PREFIX + System.currentTimeMillis()
        return CustomTheme(
            id = id,
            name = "Custom (${baseTheme.name})",
            background = edits["background"] ?: baseTheme.background,
            keyBackground = edits["keyBackground"] ?: baseTheme.keyBackground,
            keyModifierBackground = edits["keyModifierBackground"] ?: baseTheme.keyModifierBackground,
            keyText = edits["keyText"] ?: baseTheme.keyText,
            keyAccent = edits["keyAccent"] ?: baseTheme.keyAccent,
            suggestionBg = edits["suggestionBg"] ?: baseTheme.suggestionBg,
            suggestionText = edits["suggestionText"] ?: baseTheme.suggestionText,
            gestureTrail = edits["gestureTrail"] ?: baseTheme.gestureTrail
        )
    }

    fun save(theme: CustomTheme) {
        val json = org.json.JSONObject().apply {
            put("id", theme.id)
            put("name", theme.name)
            put("bg", theme.background)
            put("kbg", theme.keyBackground)
            put("kmbg", theme.keyModifierBackground)
            put("kt", theme.keyText)
            put("ka", theme.keyAccent)
            put("sbg", theme.suggestionBg)
            put("st", theme.suggestionText)
            put("gt", theme.gestureTrail)
        }
        prefs.edit().putString(theme.id, json.toString()).apply()
    }

    fun load(id: String): CustomTheme? {
        val raw = prefs.getString(id, null) ?: return null
        return runCatching {
            val obj = org.json.JSONObject(raw)
            CustomTheme(
                id = obj.getString("id"),
                name = obj.getString("name"),
                background = obj.getInt("bg"),
                keyBackground = obj.getInt("kbg"),
                keyModifierBackground = obj.getInt("kmbg"),
                keyText = obj.getInt("kt"),
                keyAccent = obj.getInt("ka"),
                suggestionBg = obj.getInt("sbg"),
                suggestionText = obj.getInt("st"),
                gestureTrail = obj.getInt("gt")
            )
        }.getOrNull()
    }

    fun listCustom(): List<CustomTheme> {
        val all = prefs.all.keys.filter { it.startsWith(TypedDataStores.CUSTOM_THEME_PREFIX) }
        return all.mapNotNull { load(it) }
    }

    fun delete(id: String) {
        prefs.edit().remove(id).apply()
    }
}

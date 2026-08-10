package com.openswift.keyboard.data

import android.content.Context
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class DataPortability(private val context: Context) {

    enum class ImportMode { MERGE, REPLACE }

    data class ImportSummary(
        val dictionaryWords: Int,
        val snippets: Int,
        val customThemes: Int
    ) {
        fun asMessage(): String =
            "Imported $dictionaryWords learned words, $snippets snippets, $customThemes themes"
    }

    fun exportJson(): String {
        val root = JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("exportedAt", Instant.now().toString())
            .put("userDictionaries", exportDictionaries())
            .put("snippets", snippetPrefs().getString(DATA_KEY, "[]")?.toJsonArray() ?: JSONArray())
            .put("customThemes", exportCustomThemes())
        return root.toString(2)
    }

    fun importJson(raw: String, mode: ImportMode): ImportSummary {
        val root = JSONObject(raw)
        require(root.optInt("schemaVersion", SCHEMA_VERSION) == SCHEMA_VERSION) {
            "Unsupported export schema"
        }

        val importedDictionaries = root.optJSONObject("userDictionaries") ?: JSONObject()
        val importedSnippets = root.optJSONArray("snippets") ?: JSONArray()
        val importedThemes = root.optJSONArray("customThemes") ?: JSONArray()

        importDictionaries(importedDictionaries, mode)
        importSnippets(importedSnippets, mode)
        importThemes(importedThemes, mode)

        return ImportSummary(
            dictionaryWords = countDictionaryWords(importedDictionaries),
            snippets = importedSnippets.length(),
            customThemes = importedThemes.length()
        )
    }

    private fun exportDictionaries(): JSONObject {
        val dictionaries = JSONObject()
        KeyboardLanguages.all.forEach { language ->
            val raw = dictionaryPrefs(language.code).getString(DATA_KEY, null)
            if (!raw.isNullOrBlank()) {
                dictionaries.put(language.code, raw.toJsonObject())
            }
        }
        return dictionaries
    }

    private fun importDictionaries(imported: JSONObject, mode: ImportMode) {
        KeyboardLanguages.all.forEach { language ->
            val prefs = dictionaryPrefs(language.code)
            val incoming = imported.optJSONObject(language.code) ?: JSONObject()
            val finalJson = when (mode) {
                ImportMode.MERGE -> mergeDictionaryJson(
                    prefs.getString(DATA_KEY, null)?.toJsonObject() ?: JSONObject(),
                    incoming
                )
                ImportMode.REPLACE -> incoming
            }
            prefs.edit().putString(DATA_KEY, finalJson.toString()).apply()
        }
    }

    private fun importSnippets(imported: JSONArray, mode: ImportMode) {
        val prefs = snippetPrefs()
        val current = prefs.getString(DATA_KEY, "[]")?.toJsonArray() ?: JSONArray()
        val finalJson = when (mode) {
            ImportMode.MERGE -> mergeSnippetJson(current, imported)
            ImportMode.REPLACE -> imported
        }
        prefs.edit().putString(DATA_KEY, finalJson.toString()).apply()
    }

    private fun exportCustomThemes(): JSONArray {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val themes = JSONArray()
        prefs.all.keys.sorted().filter { it.startsWith(CUSTOM_THEME_PREFIX) }.forEach { key ->
            val raw = prefs.getString(key, null)
            if (!raw.isNullOrBlank()) {
                themes.put(raw.toJsonObject())
            }
        }
        return themes
    }

    private fun importThemes(imported: JSONArray, mode: ImportMode) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        if (mode == ImportMode.REPLACE) {
            prefs.all.keys.filter { it.startsWith(CUSTOM_THEME_PREFIX) }.forEach { editor.remove(it) }
        }
        for (i in 0 until imported.length()) {
            val theme = imported.getJSONObject(i)
            val id = theme.getString("id")
            require(id.startsWith(CUSTOM_THEME_PREFIX)) { "Invalid custom theme id" }
            editor.putString(id, theme.toString())
        }
        editor.apply()
    }

    private fun dictionaryPrefs(languageCode: String) =
        context.getSharedPreferences(dictionaryPrefsName(languageCode), Context.MODE_PRIVATE)

    private fun snippetPrefs() =
        context.getSharedPreferences(SNIPPET_PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val SCHEMA_VERSION = 1
        private const val DATA_KEY = "data"
        private const val SNIPPET_PREFS = "snippets"
        private const val CUSTOM_THEME_PREFIX = "custom_"

        fun dictionaryPrefsName(languageCode: String): String =
            if (languageCode == "en") "user_dict" else "user_dict_$languageCode"

        fun mergeDictionaryJson(current: JSONObject, imported: JSONObject): JSONObject {
            val merged = JSONObject()
            merged.put("u", mergeCountObjects(current.optJSONObject("u"), imported.optJSONObject("u")))

            val bigrams = JSONObject()
            val keys = linkedSetOf<String>()
            current.optJSONObject("b")?.keys()?.forEach { keys.add(it) }
            imported.optJSONObject("b")?.keys()?.forEach { keys.add(it) }
            keys.forEach { previous ->
                bigrams.put(
                    previous,
                    mergeCountObjects(
                        current.optJSONObject("b")?.optJSONObject(previous),
                        imported.optJSONObject("b")?.optJSONObject(previous)
                    )
                )
            }
            merged.put("b", bigrams)
            return merged
        }

        fun mergeSnippetJson(current: JSONArray, imported: JSONArray): JSONArray =
            mergeArrayByKey(current, imported, "t")

        fun mergeThemeJson(current: JSONArray, imported: JSONArray): JSONArray =
            mergeArrayByKey(current, imported, "id")

        private fun mergeCountObjects(current: JSONObject?, imported: JSONObject?): JSONObject {
            val merged = JSONObject()
            current?.keys()?.forEach { key -> merged.put(key, current.optInt(key, 0)) }
            imported?.keys()?.forEach { key ->
                merged.put(key, merged.optInt(key, 0) + imported.optInt(key, 0))
            }
            return merged
        }

        private fun mergeArrayByKey(current: JSONArray, imported: JSONArray, keyName: String): JSONArray {
            val ordered = linkedMapOf<String, JSONObject>()
            for (i in 0 until current.length()) {
                val item = current.getJSONObject(i)
                ordered[item.getString(keyName)] = item
            }
            for (i in 0 until imported.length()) {
                val item = imported.getJSONObject(i)
                ordered[item.getString(keyName)] = item
            }
            return JSONArray().also { arr -> ordered.values.forEach { arr.put(it) } }
        }

        private fun countDictionaryWords(dictionaries: JSONObject): Int {
            var count = 0
            dictionaries.keys().forEach { language ->
                count += dictionaries.optJSONObject(language)?.optJSONObject("u")?.length() ?: 0
            }
            return count
        }

        private fun String.toJsonObject(): JSONObject = JSONObject(this)
        private fun String.toJsonArray(): JSONArray = JSONArray(this)
    }
}

package com.openswift.keyboard.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.layout.CustomLayoutStore
import java.time.Instant
import java.util.Locale
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class DataPortabilityException(message: String, cause: Throwable? = null) :
    IllegalArgumentException(message, cause)

class DataPortability(private val context: Context) {

    enum class ImportMode { MERGE, REPLACE }

    enum class Scope(internal val includesLayouts: Boolean) {
        FULL_BACKUP(true),
        ENCRYPTED_SYNC(false),
    }

    data class ImportSummary(
        val dictionaryWords: Int,
        val snippets: Int,
        val customThemes: Int,
        val customLayouts: Int,
    ) {
        fun asMessage(): String =
            "Imported $dictionaryWords learned words, $snippets snippets, " +
                "$customThemes themes, $customLayouts layouts"
    }

    fun exportJson(scope: Scope = Scope.FULL_BACKUP): String {
        val root = JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("exportedAt", Instant.now().toString())
            .put("userDictionaries", exportDictionaries())
            .put("snippets", exportSnippets())
            .put("customThemes", exportCustomThemes())
        if (scope.includesLayouts) root.put("customLayouts", exportCustomLayouts())

        return root.toString(2).also { exported ->
            if (exported.toByteArray(Charsets.UTF_8).size > MAX_DOCUMENT_BYTES) {
                throw DataPortabilityException("The exported data exceeds the 4 MiB limit.")
            }
        }
    }

    fun importJson(
        raw: String,
        mode: ImportMode,
        scope: Scope = Scope.FULL_BACKUP,
    ): ImportSummary {
        // Parse and validate the complete document before constructing any preference edits.
        val imported = parseImport(raw, scope)
        val finalDictionaries = KeyboardLanguages.all.associate { language ->
            val current = readDictionary(language.code)
            val incoming = imported.userDictionaries.optJSONObject(language.code) ?: emptyDictionary()
            language.code to when (mode) {
                ImportMode.MERGE -> mergeDictionaryJson(current, incoming)
                ImportMode.REPLACE -> incoming
            }
        }
        val finalSnippets = when (mode) {
            ImportMode.MERGE -> mergeSnippetJson(exportSnippets(), imported.snippets)
            ImportMode.REPLACE -> imported.snippets
        }
        val finalThemes = when (mode) {
            ImportMode.MERGE -> mergeThemeJson(exportCustomThemes(), imported.customThemes)
            ImportMode.REPLACE -> imported.customThemes
        }
        val finalLayouts = if (scope.includesLayouts) {
            when (mode) {
                ImportMode.MERGE -> mergeLayoutJson(exportCustomLayouts(), imported.customLayouts)
                ImportMode.REPLACE -> imported.customLayouts
            }
        } else {
            null
        }

        val mutations = buildList {
            finalDictionaries.forEach { (languageCode, dictionary) ->
                add(
                    stringMutation(
                        dictionaryPrefs(languageCode),
                        DATA_KEY,
                        dictionary.toString(),
                        "$languageCode learned dictionary",
                    ),
                )
            }
            add(stringMutation(snippetPrefs(), DATA_KEY, finalSnippets.toString(), "snippets"))
            add(
                prefixedMutation(
                    customThemePrefs(),
                    TypedDataStores.CUSTOM_THEME_PREFIX,
                    arrayById(finalThemes),
                    "custom themes",
                ),
            )
            if (finalLayouts != null) {
                add(
                    prefixedMutation(
                        customLayoutPrefs(),
                        CustomLayoutStore.CUSTOM_LAYOUT_PREFIX,
                        arrayById(finalLayouts),
                        "custom layouts",
                    ),
                )
            }
        }
        commitMutations(mutations)
        repairSelectionsAfterReplace(mode, finalThemes, finalLayouts)

        return ImportSummary(
            dictionaryWords = countDictionaryWords(imported.userDictionaries),
            snippets = imported.snippets.length(),
            customThemes = imported.customThemes.length(),
            customLayouts = imported.customLayouts.length(),
        )
    }

    private fun exportDictionaries(): JSONObject = JSONObject().also { dictionaries ->
        KeyboardLanguages.all.forEach { language ->
            val raw = dictionaryPrefs(language.code).getString(DATA_KEY, null)
            if (!raw.isNullOrBlank()) {
                val parsed = try {
                    JSONObject(raw)
                } catch (error: JSONException) {
                    throw DataPortabilityException(
                        "Stored ${language.name} learned data is not valid JSON.",
                        error,
                    )
                }
                dictionaries.put(
                    language.code,
                    validateDictionary(parsed, "stored ${language.name} dictionary"),
                )
            }
        }
    }

    private fun readDictionary(languageCode: String): JSONObject {
        val raw = dictionaryPrefs(languageCode).getString(DATA_KEY, null) ?: return emptyDictionary()
        return try {
            validateDictionary(JSONObject(raw), "stored $languageCode dictionary")
        } catch (error: JSONException) {
            throw DataPortabilityException("Stored $languageCode learned data is not valid JSON.", error)
        }
    }

    private fun exportSnippets(): JSONArray {
        val raw = snippetPrefs().getString(DATA_KEY, "[]") ?: "[]"
        return try {
            validateSnippets(JSONArray(raw), "stored snippets")
        } catch (error: JSONException) {
            throw DataPortabilityException("Stored snippets are not valid JSON.", error)
        }
    }

    private fun exportCustomThemes(): JSONArray {
        val prefs = customThemePrefs()
        return JSONArray().also { themes ->
            prefs.all.keys
                .asSequence()
                .filter { it.startsWith(TypedDataStores.CUSTOM_THEME_PREFIX) }
                .sorted()
                .forEachIndexed { index, key ->
                    val raw = prefs.getString(key, null)
                        ?: throw DataPortabilityException("Stored custom theme $key is missing.")
                    val parsed = try {
                        JSONObject(raw)
                    } catch (error: JSONException) {
                        throw DataPortabilityException("Stored custom theme $key is not valid JSON.", error)
                    }
                    val theme = validateTheme(parsed, "stored themes[$index]")
                    if (theme.getString("id") != key) {
                        throw DataPortabilityException("Stored custom theme id does not match key $key.")
                    }
                    themes.put(theme)
                }
        }
    }

    private fun exportCustomLayouts(): JSONArray {
        val prefs = customLayoutPrefs()
        return JSONArray().also { layouts ->
            prefs.all.keys
                .asSequence()
                .filter { it.startsWith(CustomLayoutStore.CUSTOM_LAYOUT_PREFIX) }
                .sorted()
                .forEachIndexed { index, key ->
                    val raw = prefs.getString(key, null)
                        ?: throw DataPortabilityException("Stored custom layout $key is missing.")
                    val layout = validateLayout(jsonObjectOrError(raw, "Stored custom layout $key"), index)
                    if (layout.getString("id") != key) {
                        throw DataPortabilityException("Stored custom layout id does not match key $key.")
                    }
                    layouts.put(layout)
                }
        }
    }

    private fun repairSelectionsAfterReplace(
        mode: ImportMode,
        themes: JSONArray,
        layouts: JSONArray?,
    ) {
        if (mode != ImportMode.REPLACE) return
        val settings = Settings(context)
        val themeIds = arrayIds(themes)
        if (settings.theme.startsWith(TypedDataStores.CUSTOM_THEME_PREFIX) && settings.theme !in themeIds) {
            settings.theme = "amoled"
        }
        if (layouts != null) {
            val layoutIds = arrayIds(layouts)
            if (settings.layout.startsWith(CustomLayoutStore.CUSTOM_LAYOUT_PREFIX) &&
                settings.layout !in layoutIds
            ) {
                settings.layout = KeyboardLanguages.byCode(settings.language).layoutId
            }
        }
    }

    private fun dictionaryPrefs(languageCode: String) =
        SecurePreferences.open(context, dictionaryPrefsName(languageCode))

    private fun snippetPrefs() = SecurePreferences.open(context, TypedDataStores.SNIPPETS)

    private fun customLayoutPrefs() = SecurePreferences.open(context, TypedDataStores.CUSTOM_LAYOUTS)

    private fun customThemePrefs() = SecurePreferences.open(
        context = context,
        storeName = TypedDataStores.CUSTOM_THEMES,
        legacyPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        migrateKey = { it.startsWith(TypedDataStores.CUSTOM_THEME_PREFIX) },
    )

    private data class PreferenceMutation(
        val label: String,
        val write: () -> Boolean,
        val rollback: () -> Unit,
    )

    private fun stringMutation(
        preferences: SharedPreferences,
        key: String,
        value: String,
        label: String,
    ): PreferenceMutation {
        val contained = preferences.contains(key)
        val previous = preferences.getString(key, null)
        return PreferenceMutation(
            label = label,
            write = { preferences.edit().putString(key, value).commit() },
            rollback = {
                val editor = preferences.edit()
                if (contained) editor.putString(key, previous) else editor.remove(key)
                editor.commit()
            },
        )
    }

    private fun prefixedMutation(
        preferences: SharedPreferences,
        prefix: String,
        values: Map<String, String>,
        label: String,
    ): PreferenceMutation {
        val previous = preferences.all
            .filterKeys { it.startsWith(prefix) }
            .mapValues { (_, value) -> value as? String }
        fun replace(replacement: Map<String, String?>): Boolean {
            val editor = preferences.edit()
            preferences.all.keys.filter { it.startsWith(prefix) }.forEach(editor::remove)
            replacement.forEach { (key, value) -> editor.putString(key, value) }
            return editor.commit()
        }
        return PreferenceMutation(
            label = label,
            write = { replace(values) },
            rollback = { replace(previous) },
        )
    }

    private fun commitMutations(mutations: List<PreferenceMutation>) {
        val committed = mutableListOf<PreferenceMutation>()
        mutations.forEach { mutation ->
            val writeResult = runCatching(mutation.write)
            if (writeResult.getOrDefault(false)) {
                committed += mutation
            } else {
                runCatching(mutation.rollback)
                committed.asReversed().forEach { previous -> runCatching(previous.rollback) }
                throw DataPortabilityException(
                    "Unable to save imported ${mutation.label}.",
                    writeResult.exceptionOrNull(),
                )
            }
        }
    }

    companion object {
        const val MAX_DOCUMENT_BYTES = 4 * 1024 * 1024
        private const val SCHEMA_VERSION = 1
        private const val DATA_KEY = "data"
        private const val MAX_SNIPPETS = 500
        private const val MAX_CUSTOM_ITEMS = 100
        private const val MAX_WORDS_PER_LANGUAGE = 20_000
        private const val MAX_BIGRAM_CONTEXTS = 20_000
        private const val MAX_BIGRAM_PAIRS = 100_000
        private const val MAX_LEARNING_COUNT = 1_000_000
        private val THEME_ID = Regex("^custom_[a-z0-9_]{1,40}$")
        private val THEME_COLOR_KEYS = listOf("bg", "kbg", "kmbg", "kt", "ka", "sbg", "st", "gt")

        internal data class ParsedImport(
            val userDictionaries: JSONObject,
            val snippets: JSONArray,
            val customThemes: JSONArray,
            val customLayouts: JSONArray,
        )

        fun dictionaryPrefsName(languageCode: String): String =
            TypedDataStores.userDictionary(languageCode)

        internal fun parseImport(raw: String, scope: Scope): ParsedImport {
            validate(raw.isNotBlank()) { "The data file is empty." }
            validate(raw.toByteArray(Charsets.UTF_8).size <= MAX_DOCUMENT_BYTES) {
                "The data file is larger than 4 MiB."
            }
            val root = try {
                JSONObject(raw)
            } catch (error: JSONException) {
                throw DataPortabilityException("The data file is not valid JSON.", error)
            }
            val schema = root.opt("schemaVersion") as? Number
                ?: throw DataPortabilityException("schemaVersion must be a number.")
            validate(schema.toInt() == SCHEMA_VERSION && schema.toDouble() == SCHEMA_VERSION.toDouble()) {
                "Unsupported data schema; this build supports version $SCHEMA_VERSION."
            }

            val dictionaries = requiredObject(root, "userDictionaries")
            val snippets = requiredArray(root, "snippets")
            val themes = requiredArray(root, "customThemes")
            if (!scope.includesLayouts && root.has("customLayouts")) {
                throw DataPortabilityException(
                    "Encrypted sync snapshots cannot contain custom layouts.",
                )
            }
            val layouts = if (scope.includesLayouts && root.has("customLayouts")) {
                requiredArray(root, "customLayouts")
            } else {
                JSONArray()
            }

            val supportedLanguages = KeyboardLanguages.all.mapTo(linkedSetOf()) { it.code }
            val normalizedDictionaries = JSONObject()
            dictionaries.keys().forEach { languageCode ->
                validate(languageCode in supportedLanguages) {
                    "userDictionaries contains unsupported language \"$languageCode\"."
                }
                val dictionary = dictionaries.optJSONObject(languageCode)
                    ?: throw DataPortabilityException(
                        "userDictionaries.$languageCode must be an object.",
                    )
                normalizedDictionaries.put(
                    languageCode,
                    validateDictionary(dictionary, "userDictionaries.$languageCode"),
                )
            }

            return ParsedImport(
                userDictionaries = normalizedDictionaries,
                snippets = validateSnippets(snippets, "snippets"),
                customThemes = validateThemes(themes),
                customLayouts = validateLayouts(layouts),
            )
        }

        fun mergeDictionaryJson(current: JSONObject, imported: JSONObject): JSONObject =
            JSONObject().apply {
                put(
                    "u",
                    mergeCountObjects(current.optJSONObject("u"), imported.optJSONObject("u")),
                )

                val bigrams = JSONObject()
                val keys = linkedSetOf<String>()
                current.optJSONObject("b")?.keys()?.forEach(keys::add)
                imported.optJSONObject("b")?.keys()?.forEach(keys::add)
                keys.forEach { previous ->
                    bigrams.put(
                        previous,
                        mergeCountObjects(
                            current.optJSONObject("b")?.optJSONObject(previous),
                            imported.optJSONObject("b")?.optJSONObject(previous),
                        ),
                    )
                }
                put("b", bigrams)
            }

        fun mergeSnippetJson(current: JSONArray, imported: JSONArray): JSONArray =
            mergeArrayByKey(current, imported, "t")

        fun mergeThemeJson(current: JSONArray, imported: JSONArray): JSONArray =
            mergeArrayByKey(current, imported, "id")

        fun mergeLayoutJson(current: JSONArray, imported: JSONArray): JSONArray =
            mergeArrayByKey(current, imported, "id")

        private fun validateDictionary(dictionary: JSONObject, path: String): JSONObject {
            val unigrams = optionalObject(dictionary, "u", "$path.u")
            val bigrams = optionalObject(dictionary, "b", "$path.b")
            validate(unigrams.length() <= MAX_WORDS_PER_LANGUAGE) {
                "$path.u contains more than $MAX_WORDS_PER_LANGUAGE words."
            }
            validate(bigrams.length() <= MAX_BIGRAM_CONTEXTS) {
                "$path.b contains more than $MAX_BIGRAM_CONTEXTS contexts."
            }

            val canonicalUnigrams = validateCountObject(unigrams, "$path.u")
            var bigramPairs = 0
            val canonicalBigrams = JSONObject()
            bigrams.keys().forEach { previous ->
                validateWord(previous, "$path.b context")
                val values = bigrams.optJSONObject(previous)
                    ?: throw DataPortabilityException("$path.b.$previous must be an object.")
                bigramPairs += values.length()
                validate(bigramPairs <= MAX_BIGRAM_PAIRS) {
                    "$path.b contains more than $MAX_BIGRAM_PAIRS word pairs."
                }
                canonicalBigrams.put(previous, validateCountObject(values, "$path.b.$previous"))
            }
            return JSONObject().put("u", canonicalUnigrams).put("b", canonicalBigrams)
        }

        private fun validateCountObject(values: JSONObject, path: String): JSONObject =
            JSONObject().also { canonical ->
                values.keys().forEach { word ->
                    validateWord(word, path)
                    canonical.put(word, requiredCount(values, word, "$path.$word"))
                }
            }

        private fun validateWord(word: String, path: String) {
            validate(UserDictionary.normalizeWord(word) == word) {
                "$path contains invalid or non-normalized word \"$word\"."
            }
        }

        private fun requiredCount(values: JSONObject, key: String, path: String): Int {
            val number = values.opt(key) as? Number
                ?: throw DataPortabilityException("$path must be an integer count.")
            val count = number.toLong()
            validate(number.toDouble() == count.toDouble() && count in 1..MAX_LEARNING_COUNT.toLong()) {
                "$path must be between 1 and $MAX_LEARNING_COUNT."
            }
            return count.toInt()
        }

        private fun validateSnippets(snippets: JSONArray, path: String): JSONArray {
            validate(snippets.length() <= MAX_SNIPPETS) {
                "$path contains more than $MAX_SNIPPETS snippets."
            }
            val triggers = mutableSetOf<String>()
            return JSONArray().also { canonical ->
                for (index in 0 until snippets.length()) {
                    val itemPath = "$path[$index]"
                    val item = snippets.optJSONObject(index)
                        ?: throw DataPortabilityException("$itemPath must be an object.")
                    val trigger = requiredString(item, "t", "$itemPath.t")
                    val expansion = requiredString(item, "x", "$itemPath.x", allowBlank = false)
                    SnippetManager.validateImported(trigger, expansion)?.let { error ->
                        throw DataPortabilityException("$itemPath: $error")
                    }
                    validate(triggers.add(trigger.trim().lowercase(Locale.ROOT))) {
                        "$path contains duplicate trigger \"$trigger\"."
                    }
                    canonical.put(JSONObject().put("t", trigger.trim()).put("x", expansion))
                }
            }
        }

        private fun validateThemes(themes: JSONArray): JSONArray {
            validate(themes.length() <= MAX_CUSTOM_ITEMS) {
                "customThemes contains more than $MAX_CUSTOM_ITEMS themes."
            }
            val ids = mutableSetOf<String>()
            return JSONArray().also { canonical ->
                for (index in 0 until themes.length()) {
                    val theme = themes.optJSONObject(index)
                        ?: throw DataPortabilityException("customThemes[$index] must be an object.")
                    val validated = validateTheme(theme, "customThemes[$index]")
                    val id = validated.getString("id")
                    validate(ids.add(id)) { "customThemes contains duplicate id \"$id\"." }
                    canonical.put(validated)
                }
            }
        }

        private fun validateTheme(theme: JSONObject, path: String): JSONObject {
            val id = requiredString(theme, "id", "$path.id")
            validate(THEME_ID.matches(id)) {
                "$path.id must start with custom_ and use lowercase letters, numbers, or underscores."
            }
            val name = requiredString(theme, "name", "$path.name")
            validate(name.length <= 48 && name.none { it.isISOControl() }) {
                "$path.name must contain 1-48 characters without control characters."
            }
            return JSONObject().put("id", id).put("name", name).also { canonical ->
                THEME_COLOR_KEYS.forEach { key ->
                    canonical.put(key, requiredInt(theme, key, "$path.$key"))
                }
            }
        }

        private fun validateLayouts(layouts: JSONArray): JSONArray {
            validate(layouts.length() <= MAX_CUSTOM_ITEMS) {
                "customLayouts contains more than $MAX_CUSTOM_ITEMS layouts."
            }
            val ids = mutableSetOf<String>()
            return JSONArray().also { canonical ->
                for (index in 0 until layouts.length()) {
                    val item = layouts.optJSONObject(index)
                        ?: throw DataPortabilityException("customLayouts[$index] must be an object.")
                    val layout = validateLayout(item, index)
                    val id = layout.getString("id")
                    validate(ids.add(id)) { "customLayouts contains duplicate id \"$id\"." }
                    canonical.put(layout)
                }
            }
        }

        private fun validateLayout(item: JSONObject, index: Int): JSONObject {
            val imported = try {
                CustomizationPackageParser.decodeLayout(item.toString())
            } catch (error: CustomizationPackageException) {
                throw DataPortabilityException("customLayouts[$index]: ${error.message}", error)
            }
            return JSONObject(CustomizationPackageParser.encodeLayout(imported))
        }

        private fun jsonObjectOrError(raw: String, label: String): JSONObject = try {
            JSONObject(raw)
        } catch (error: JSONException) {
            throw DataPortabilityException("$label is not valid JSON.", error)
        }

        private fun requiredObject(root: JSONObject, key: String): JSONObject =
            root.optJSONObject(key)
                ?: throw DataPortabilityException("$key must be an object.")

        private fun optionalObject(root: JSONObject, key: String, path: String): JSONObject {
            if (!root.has(key)) return JSONObject()
            return root.optJSONObject(key)
                ?: throw DataPortabilityException("$path must be an object.")
        }

        private fun requiredArray(root: JSONObject, key: String): JSONArray =
            root.optJSONArray(key)
                ?: throw DataPortabilityException("$key must be an array.")

        private fun requiredString(
            root: JSONObject,
            key: String,
            path: String,
            allowBlank: Boolean = false,
        ): String {
            val value = root.opt(key) as? String
                ?: throw DataPortabilityException("$path must be a string.")
            validate(allowBlank || value.isNotBlank()) { "$path cannot be blank." }
            return value
        }

        private fun requiredInt(root: JSONObject, key: String, path: String): Int {
            val number = root.opt(key) as? Number
                ?: throw DataPortabilityException("$path must be an integer.")
            val value = number.toLong()
            validate(
                number.toDouble() == value.toDouble() &&
                    value >= Int.MIN_VALUE.toLong() && value <= Int.MAX_VALUE.toLong(),
            ) {
                "$path must be a 32-bit integer."
            }
            return value.toInt()
        }

        private fun mergeCountObjects(current: JSONObject?, imported: JSONObject?): JSONObject =
            JSONObject().also { merged ->
                current?.keys()?.forEach { key ->
                    merged.put(key, current.optInt(key, 0).coerceIn(1, MAX_LEARNING_COUNT))
                }
                imported?.keys()?.forEach { key ->
                    val sum = merged.optLong(key, 0L) + imported.optLong(key, 0L)
                    merged.put(key, sum.coerceIn(1L, MAX_LEARNING_COUNT.toLong()).toInt())
                }
            }

        private fun mergeArrayByKey(current: JSONArray, imported: JSONArray, keyName: String): JSONArray {
            val ordered = linkedMapOf<String, JSONObject>()
            for (index in 0 until current.length()) {
                val item = current.getJSONObject(index)
                ordered[item.getString(keyName)] = item
            }
            for (index in 0 until imported.length()) {
                val item = imported.getJSONObject(index)
                ordered[item.getString(keyName)] = item
            }
            return JSONArray().also { array -> ordered.values.forEach(array::put) }
        }

        private fun arrayById(array: JSONArray): Map<String, String> = buildMap {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                put(item.getString("id"), item.toString())
            }
        }

        private fun arrayIds(array: JSONArray): Set<String> = buildSet {
            for (index in 0 until array.length()) add(array.getJSONObject(index).getString("id"))
        }

        private fun countDictionaryWords(dictionaries: JSONObject): Int {
            var count = 0
            dictionaries.keys().forEach { language ->
                count += dictionaries.optJSONObject(language)?.optJSONObject("u")?.length() ?: 0
            }
            return count
        }

        private fun emptyDictionary(): JSONObject =
            JSONObject().put("u", JSONObject()).put("b", JSONObject())

        private inline fun validate(condition: Boolean, message: () -> String) {
            if (!condition) throw DataPortabilityException(message())
        }
    }
}

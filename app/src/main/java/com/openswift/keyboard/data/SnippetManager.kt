package com.openswift.keyboard.data

import android.content.Context
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

internal interface SnippetStorage {
    fun read(): String
    fun write(value: String)
}

private class PreferencesSnippetStorage(ctx: Context) : SnippetStorage {
    private val prefs = SecurePreferences.open(ctx, TypedDataStores.SNIPPETS)

    override fun read(): String = prefs.getString(DATA_KEY, "[]") ?: "[]"

    override fun write(value: String) {
        prefs.edit().putString(DATA_KEY, value).apply()
    }

    private companion object {
        const val DATA_KEY = "data"
    }
}

/** Validated, encrypted trigger-to-expansion storage and matching. */
class SnippetManager internal constructor(private val storage: SnippetStorage) {

    data class Snippet(val trigger: String, val text: String)

    data class Match(val trigger: String, val expansion: String)

    private val snippets: MutableList<Snippet> = mutableListOf()

    constructor(ctx: Context) : this(PreferencesSnippetStorage(ctx))

    init {
        reload()
    }

    /**
     * Creates or edits a snippet. Returns a user-facing validation message, or null on success.
     */
    fun save(originalTrigger: String?, trigger: String, text: String): String? {
        val normalizedTrigger = normalizeTrigger(trigger)
        val validationError = validate(normalizedTrigger, text, originalTrigger)
        if (validationError != null) return validationError

        val originalKey = originalTrigger?.let(::normalizeTrigger)
        val snippet = Snippet(normalizedTrigger, text)
        val originalIndex = originalKey?.let { key ->
            snippets.indexOfFirst { canonical(it.trigger) == canonical(key) }.takeIf { it >= 0 }
        }

        if (originalIndex != null) {
            snippets[originalIndex] = snippet
        } else {
            snippets.add(snippet)
        }
        persist()
        return null
    }

    fun add(trigger: String, text: String): Boolean = save(null, trigger, text) == null

    fun remove(trigger: String): Boolean {
        val removed = snippets.removeAll { canonical(it.trigger) == canonical(trigger) }
        if (removed) persist()
        return removed
    }

    fun expand(trigger: String): String? {
        val key = canonical(normalizeTrigger(trigger))
        return snippets.firstOrNull { canonical(it.trigger) == key }?.text
    }

    /** Finds the longest trigger ending at the current token boundary. */
    fun matchEnding(buffer: CharSequence): Match? {
        if (buffer.isEmpty()) return null
        val candidateText = buffer.toString().lowercase(Locale.ROOT)
        return snippets
            .asSequence()
            .sortedByDescending { it.trigger.length }
            .firstOrNull { snippet ->
                val trigger = canonical(snippet.trigger)
                if (!candidateText.endsWith(trigger)) return@firstOrNull false
                val start = candidateText.length - trigger.length
                start == 0 || !trigger.first().isLetterOrDigit() ||
                    !candidateText[start - 1].isLetterOrDigit()
            }
            ?.let { Match(it.trigger, it.text) }
    }

    fun getAll(): List<Snippet> = snippets.toList()

    fun maxTriggerLength(): Int = snippets.maxOfOrNull { it.trigger.length } ?: 0

    fun reload() {
        snippets.clear()
        runCatching {
            val values = JSONArray(storage.read())
            val seen = HashSet<String>()
            for (index in 0 until values.length()) {
                val item = values.optJSONObject(index) ?: continue
                val trigger = normalizeTrigger(item.optString(TRIGGER_KEY))
                val expansion = item.optString(EXPANSION_KEY)
                if (validateShape(trigger, expansion) != null) continue
                if (seen.add(canonical(trigger))) snippets.add(Snippet(trigger, expansion))
            }
        }
    }

    private fun validate(trigger: String, text: String, originalTrigger: String?): String? {
        validateShape(trigger, text)?.let { return it }
        val originalKey = originalTrigger?.let { canonical(normalizeTrigger(it)) }
        val duplicate = snippets.any { snippet ->
            val key = canonical(snippet.trigger)
            key == canonical(trigger) && key != originalKey
        }
        return if (duplicate) "That trigger already exists." else null
    }

    private fun persist() {
        val values = JSONArray()
        snippets.forEach { snippet ->
            values.put(
                JSONObject()
                    .put(TRIGGER_KEY, snippet.trigger)
                    .put(EXPANSION_KEY, snippet.text)
            )
        }
        storage.write(values.toString())
    }

    companion object {
        const val MAX_TRIGGER_LENGTH = 32
        const val MAX_EXPANSION_LENGTH = 4_096
        private const val TRIGGER_KEY = "t"
        private const val EXPANSION_KEY = "x"

        internal fun normalizeTrigger(raw: String): String = raw.trim()

        private fun canonical(trigger: String): String = trigger.lowercase(Locale.ROOT)

        private fun validateShape(trigger: String, text: String): String? = when {
            trigger.isEmpty() -> "Enter a trigger."
            trigger.length > MAX_TRIGGER_LENGTH ->
                "Trigger must be $MAX_TRIGGER_LENGTH characters or fewer."
            trigger.any { it.isWhitespace() || it.isISOControl() } ->
                "Trigger cannot contain spaces or control characters."
            text.isBlank() -> "Enter replacement text."
            text.length > MAX_EXPANSION_LENGTH ->
                "Replacement text must be $MAX_EXPANSION_LENGTH characters or fewer."
            else -> null
        }
    }
}

/** Keeps sensitive-field gating testable outside InputMethodService. */
internal object SnippetExpansionPolicy {
    fun match(
        snippets: SnippetManager,
        buffer: CharSequence,
        privateField: Boolean
    ): SnippetManager.Match? = if (privateField) null else snippets.matchEnding(buffer)
}

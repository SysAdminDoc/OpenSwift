package com.openswift.keyboard.engine

import android.content.Context
import com.openswift.keyboard.data.SecurePreferences
import com.openswift.keyboard.data.TypedDataStores
import java.util.Locale
import org.json.JSONObject

internal interface UserDictionaryStorage {
    fun read(): String?
    fun write(value: String)
}

private class PreferencesUserDictionaryStorage(ctx: Context, languageCode: String) :
    UserDictionaryStorage {
    private val prefs = SecurePreferences.open(ctx, TypedDataStores.userDictionary(languageCode))

    override fun read(): String? = prefs.getString(DATA_KEY, null)

    override fun write(value: String) {
        prefs.edit().putString(DATA_KEY, value).apply()
    }

    private companion object {
        const val DATA_KEY = "data"
    }
}

/**
 * Per-user n-gram store. Unigrams and bigrams are persisted as compact JSON in the
 * language-specific encrypted typed-data store.
 */
class UserDictionary internal constructor(private val storage: UserDictionaryStorage) {

    private val unigram: HashMap<String, Int> = HashMap()
    private val bigram: HashMap<String, HashMap<String, Int>> = HashMap()

    constructor(ctx: Context, languageCode: String = "en") :
        this(PreferencesUserDictionaryStorage(ctx, languageCode))

    init {
        load()
    }

    fun getWordCount(): Int = unigram.size

    fun learn(prev: String?, word: String) {
        val normalizedWord = normalizeWord(word) ?: return
        unigram[normalizedWord] = (unigram[normalizedWord] ?: 0) + 1

        normalizeWord(prev)?.let { normalizedPrevious ->
            val inner = bigram.getOrPut(normalizedPrevious) { HashMap() }
            inner[normalizedWord] = (inner[normalizedWord] ?: 0) + 1
        }
        if (unigram.size % AUTO_SAVE_WORD_INTERVAL == 0) save()
    }

    fun unigramCount(word: String): Int = normalizeWord(word)?.let { unigram[it] } ?: 0

    fun bigramCount(previousWord: String, word: String): Int {
        val previous = normalizeWord(previousWord) ?: return 0
        val next = normalizeWord(word) ?: return 0
        return bigram[previous]?.get(next) ?: 0
    }

    fun nextAfter(previousWord: String, limit: Int = 5): List<String> {
        if (limit <= 0) return emptyList()
        val previous = normalizeWord(previousWord) ?: return emptyList()
        return bigram[previous]
            .orEmpty()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { it.key }
    }

    fun wordsStartingWith(prefix: String, limit: Int = Int.MAX_VALUE): List<String> {
        if (limit <= 0) return emptyList()
        val normalizedPrefix = prefix.trim().lowercase(Locale.ROOT)
        return unigram.entries
            .asSequence()
            .filter { it.key.startsWith(normalizedPrefix) }
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { it.key }
            .toList()
    }

    internal fun knownWords(): Set<String> = unigram.keys

    fun isKnown(word: String): Boolean = normalizeWord(word)?.let { unigram.containsKey(it) } == true

    fun reset() {
        unigram.clear()
        bigram.clear()
        save()
    }

    fun save() {
        val root = JSONObject()
        val unigrams = JSONObject()
        unigram.forEach { (word, count) -> unigrams.put(word, count) }
        root.put("u", unigrams)

        val bigrams = JSONObject()
        bigram.forEach { (previous, inner) ->
            val values = JSONObject()
            inner.forEach { (next, count) -> values.put(next, count) }
            bigrams.put(previous, values)
        }
        root.put("b", bigrams)
        storage.write(root.toString())
    }

    private fun load() {
        val raw = storage.read() ?: return
        runCatching {
            val root = JSONObject(raw)
            val unigrams = root.optJSONObject("u") ?: JSONObject()
            unigrams.keys().forEach { rawWord ->
                normalizeWord(rawWord)?.let { word ->
                    val count = unigrams.optInt(rawWord, 1).coerceAtLeast(1)
                    unigram[word] = (unigram[word] ?: 0) + count
                }
            }

            val bigrams = root.optJSONObject("b") ?: JSONObject()
            bigrams.keys().forEach { rawPrevious ->
                val previous = normalizeWord(rawPrevious) ?: return@forEach
                val values = bigrams.optJSONObject(rawPrevious) ?: return@forEach
                values.keys().forEach { rawNext ->
                    val next = normalizeWord(rawNext) ?: return@forEach
                    val inner = bigram.getOrPut(previous) { HashMap() }
                    val count = values.optInt(rawNext, 1).coerceAtLeast(1)
                    inner[next] = (inner[next] ?: 0) + count
                }
            }
        }
    }

    companion object {
        private const val AUTO_SAVE_WORD_INTERVAL = 8
        private const val MAX_WORD_LENGTH = 32
        private val WORD_CONNECTORS = setOf('\'', '’', '-')

        internal fun normalizeWord(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            val normalized = raw
                .trim()
                .trim { !it.isLetter() && it !in WORD_CONNECTORS }
                .lowercase(Locale.ROOT)
            if (normalized.isEmpty() || normalized.length > MAX_WORD_LENGTH) return null
            if (!normalized.first().isLetter() || !normalized.last().isLetter()) return null
            if (normalized.any { !it.isLetter() && it !in WORD_CONNECTORS }) return null
            return normalized
        }
    }
}

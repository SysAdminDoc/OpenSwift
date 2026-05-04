package com.openswift.keyboard.engine

import android.content.Context
import org.json.JSONObject

/**
 * Per-user n-gram store. Stores unigram counts of typed words and a small bigram
 * map of (prevWord -> nextWord -> count) so the IME can do better next-word prediction
 * over time. Stored as JSON in SharedPreferences (small enough for normal usage).
 */
class UserDictionary(ctx: Context) {

    private val prefs = ctx.getSharedPreferences("user_dict", Context.MODE_PRIVATE)
    private val unigram: HashMap<String, Int> = HashMap()
    private val bigram: HashMap<String, HashMap<String, Int>> = HashMap()

    init { load() }

    fun learn(prev: String?, word: String) {
        if (word.isBlank() || word.length > 32) return
        val w = word.lowercase()
        unigram[w] = (unigram[w] ?: 0) + 1
        if (!prev.isNullOrBlank()) {
            val p = prev.lowercase()
            val inner = bigram.getOrPut(p) { HashMap() }
            inner[w] = (inner[w] ?: 0) + 1
        }
        if (unigram.size % 8 == 0) save()
    }

    fun unigramCount(w: String): Int = unigram[w.lowercase()] ?: 0

    fun nextAfter(prev: String, limit: Int = 5): List<String> {
        val inner = bigram[prev.lowercase()] ?: return emptyList()
        return inner.entries.sortedByDescending { it.value }.take(limit).map { it.key }
    }

    fun isKnown(w: String): Boolean = unigram.containsKey(w.lowercase())

    private fun load() {
        val raw = prefs.getString("data", null) ?: return
        runCatching {
            val root = JSONObject(raw)
            val u = root.optJSONObject("u") ?: JSONObject()
            u.keys().forEach { k -> unigram[k] = u.optInt(k, 1) }
            val b = root.optJSONObject("b") ?: JSONObject()
            b.keys().forEach { p ->
                val inner = b.getJSONObject(p)
                val map = HashMap<String, Int>()
                inner.keys().forEach { n -> map[n] = inner.optInt(n, 1) }
                bigram[p] = map
            }
        }
    }

    fun save() {
        val root = JSONObject()
        val u = JSONObject()
        unigram.forEach { (k, v) -> u.put(k, v) }
        root.put("u", u)
        val b = JSONObject()
        bigram.forEach { (p, inner) ->
            val o = JSONObject()
            inner.forEach { (n, c) -> o.put(n, c) }
            b.put(p, o)
        }
        root.put("b", b)
        prefs.edit().putString("data", root.toString()).apply()
    }
}

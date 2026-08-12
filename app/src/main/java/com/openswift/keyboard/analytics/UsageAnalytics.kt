package com.openswift.keyboard.analytics

import android.content.Context
import android.content.SharedPreferences
import com.openswift.keyboard.data.SecurePreferences
import com.openswift.keyboard.data.TypedDataStores

/**
 * Simple in-device analytics: tracks usage patterns to improve prediction.
 * No cloud upload. Data stored locally in SharedPreferences.
 */
class UsageAnalytics(ctx: Context) {

    private val prefs: SharedPreferences = SecurePreferences.open(ctx, TypedDataStores.ANALYTICS)

    fun recordKeyPress(key: String) {
        val count = prefs.getInt("key_$key", 0)
        prefs.edit().putInt("key_$key", count + 1).apply()
    }

    fun recordWord(word: String) {
        val count = prefs.getInt("word_$word", 0)
        prefs.edit().putInt("word_$word", count + 1).apply()
    }

    fun recordGlide(word: String) {
        val count = prefs.getInt("glide_$word", 0)
        prefs.edit().putInt("glide_$word", count + 1).apply()
    }

    fun recordCorrection(original: String, corrected: String) {
        val count = prefs.getInt("correct_${original}_to_$corrected", 0)
        prefs.edit().putInt("correct_${original}_to_$corrected", count + 1).apply()
    }

    fun getTopKeys(limit: Int = 10): List<Pair<String, Int>> {
        return prefs.all
            .filter { it.key.startsWith("key_") }
            .map { (k, v) -> k.removePrefix("key_") to (v as? Int ?: 0) }
            .sortedByDescending { it.second }
            .take(limit)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

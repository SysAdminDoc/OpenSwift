package com.openswift.keyboard.engine

import android.content.Context
import com.openswift.keyboard.R

/** Loads the static word frequency list shipped with the app and a per-user dictionary. */
class WordList(ctx: Context) {

    val frequencies: Map<String, Int>
    val words: List<String>

    init {
        val freq = HashMap<String, Int>(8192)
        ctx.resources.openRawResource(R.raw.words).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split('\t', limit = 2)
                if (parts.size == 2) {
                    val w = parts[0]
                    val f = parts[1].toIntOrNull() ?: 1
                    freq[w] = f
                }
            }
        }
        frequencies = freq
        words = freq.keys.sortedByDescending { freq[it] }
    }

    fun frequency(word: String): Int = frequencies[word.lowercase()] ?: 0

    fun contains(word: String): Boolean = frequencies.containsKey(word.lowercase())
}

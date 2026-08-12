package com.openswift.keyboard.engine

import android.content.Context
import com.openswift.keyboard.R
import java.util.Locale

/** Loads and indexes the static word-frequency list shipped with the app. */
class WordList private constructor(rawFrequencies: Map<String, Int>) {

    val frequencies: Map<String, Int>
    val words: List<String>

    private val alphabeticalWords: List<String>
    private val wordsByLength: Map<Int, List<String>>

    init {
        val normalized = HashMap<String, Int>(rawFrequencies.size)
        rawFrequencies.forEach { (rawWord, rawFrequency) ->
            val word = rawWord.trim().lowercase(Locale.ROOT)
            if (word.isNotEmpty()) {
                normalized[word] = maxOf(normalized[word] ?: 0, rawFrequency.coerceAtLeast(1))
            }
        }
        frequencies = normalized
        words = normalized.keys.sortedWith(
            compareByDescending<String> { normalized.getValue(it) }.thenBy { it }
        )
        alphabeticalWords = normalized.keys.sorted()
        wordsByLength = words.groupBy { it.length }
    }

    constructor(ctx: Context, wordListRes: Int = R.raw.words) : this(load(ctx, wordListRes))

    fun frequency(word: String): Int = frequencies[word.lowercase(Locale.ROOT)] ?: 0

    fun contains(word: String): Boolean = frequencies.containsKey(word.lowercase(Locale.ROOT))

    /** Returns only the indexed lexical range matching [prefix], not the full dictionary. */
    fun prefixMatches(prefix: String, limit: Int = Int.MAX_VALUE): List<String> {
        if (limit <= 0) return emptyList()
        val normalizedPrefix = prefix.lowercase(Locale.ROOT)
        if (normalizedPrefix.isEmpty()) return words.take(limit)

        val start = lowerBound(alphabeticalWords, normalizedPrefix)
        val matches = ArrayList<String>(minOf(limit, 64))
        for (index in start until alphabeticalWords.size) {
            val word = alphabeticalWords[index]
            if (!word.startsWith(normalizedPrefix)) break
            matches.add(word)
            if (matches.size == limit) break
        }
        return matches
    }

    internal fun wordsWithinLength(length: Int, budget: Int): Sequence<String> {
        val minimum = (length - budget).coerceAtLeast(1)
        val maximum = length + budget
        return (minimum..maximum).asSequence().flatMap { candidateLength ->
            wordsByLength[candidateLength].orEmpty().asSequence()
        }
    }

    companion object {
        internal fun fromEntries(entries: Map<String, Int>): WordList = WordList(entries)

        internal fun parseLines(lines: Sequence<String>): Map<String, Int> {
            val frequencies = HashMap<String, Int>()
            lines.forEach { line ->
                val parts = line.split('\t', limit = 2)
                if (parts.size != 2) return@forEach
                val word = parts[0].trim().lowercase(Locale.ROOT)
                val frequency = parts[1].trim().toIntOrNull() ?: return@forEach
                if (word.isNotEmpty()) {
                    frequencies[word] = maxOf(frequencies[word] ?: 0, frequency.coerceAtLeast(1))
                }
            }
            return frequencies
        }

        private fun load(ctx: Context, wordListRes: Int): Map<String, Int> =
            ctx.resources.openRawResource(wordListRes).bufferedReader().useLines(::parseLines)

        private fun lowerBound(values: List<String>, target: String): Int {
            var low = 0
            var high = values.size
            while (low < high) {
                val middle = (low + high) ushr 1
                if (values[middle] < target) low = middle + 1 else high = middle
            }
            return low
        }
    }
}

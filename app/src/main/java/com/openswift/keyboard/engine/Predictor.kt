package com.openswift.keyboard.engine

import kotlin.math.max
import kotlin.math.min

/**
 * Generates suggestions for the current word using:
 *  - exact prefix match (top by frequency)
 *  - fuzzy match (Damerau-Levenshtein distance <= edit budget)
 *  - bigram boost from the user dictionary based on the previous word
 */
class Predictor(
    private val wordList: WordList,
    private val userDict: UserDictionary
) {

    /** Returns up to [limit] suggestions, best first. */
    fun suggest(prefix: String, previousWord: String?, limit: Int = 3): List<String> {
        val p = prefix.lowercase()
        if (p.isEmpty()) {
            // No current word - return likely next words from bigrams, else top common words.
            val byBigram = previousWord?.let { userDict.nextAfter(it, limit) }.orEmpty()
            if (byBigram.isNotEmpty()) return byBigram
            return wordList.words.take(limit)
        }

        val budget = when {
            p.length <= 3 -> 1
            p.length <= 6 -> 2
            else -> 3
        }

        data class Cand(val word: String, val score: Double)
        val results = ArrayList<Cand>(64)

        for (w in wordList.words) {
            if (w.length > p.length + budget + 4) continue
            val score = scoreCandidate(p, w, previousWord, budget) ?: continue
            results.add(Cand(w, score))
        }
        for (w in userDict.let { listOf<String>() } + listOf<String>()) { /* placeholder */ }

        return results.sortedByDescending { it.score }.take(limit).map { it.word }
    }

    /** Best correction for an entered word, or the word itself if it's already valid/short. */
    fun autoCorrect(typed: String, previousWord: String?): String {
        if (typed.length < 3) return typed
        if (wordList.contains(typed) || userDict.isKnown(typed)) return typed
        val s = suggest(typed, previousWord, limit = 1)
        return s.firstOrNull() ?: typed
    }

    private fun scoreCandidate(prefix: String, word: String, prev: String?, budget: Int): Double? {
        val pl = prefix.length
        val wl = word.length

        // Strong score: word starts with prefix
        val baseFreq = (wordList.frequency(word) + userDict.unigramCount(word)).coerceAtLeast(1)
        val freqScore = Math.log10(baseFreq.toDouble() + 1.0)

        if (word.startsWith(prefix)) {
            // shorter completions slightly preferred
            val lenBonus = 1.0 - (wl - pl).coerceAtMost(8) * 0.02
            val bigramBonus = if (prev != null && userDict.nextAfter(prev).contains(word)) 1.5 else 0.0
            return 5.0 + freqScore + lenBonus + bigramBonus
        }

        if (kotlin.math.abs(wl - pl) > budget) return null
        val d = damerauLevenshtein(prefix, word, budget)
        if (d < 0 || d > budget) return null
        val bigramBonus = if (prev != null && userDict.nextAfter(prev).contains(word)) 1.0 else 0.0
        return (budget - d).toDouble() + freqScore * 0.5 + bigramBonus
    }

    /**
     * Bounded Damerau-Levenshtein distance. Returns -1 if the distance exceeds [maxDist].
     */
    private fun damerauLevenshtein(a: String, b: String, maxDist: Int): Int {
        val n = a.length; val m = b.length
        if (kotlin.math.abs(n - m) > maxDist) return -1
        val prev2 = IntArray(m + 1)
        val prev1 = IntArray(m + 1) { it }
        val cur = IntArray(m + 1)
        for (i in 1..n) {
            cur[0] = i
            var rowMin = cur[0]
            val from = max(1, i - maxDist)
            val to = min(m, i + maxDist)
            if (from > 1) cur[from - 1] = maxDist + 1
            for (j in from..to) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                var v = min(min(cur[j - 1] + 1, prev1[j] + 1), prev1[j - 1] + cost)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    v = min(v, prev2[j - 2] + 1)
                }
                cur[j] = v
                if (v < rowMin) rowMin = v
            }
            if (rowMin > maxDist) return -1
            System.arraycopy(prev1, 0, prev2, 0, m + 1)
            System.arraycopy(cur, 0, prev1, 0, m + 1)
        }
        val d = prev1[m]
        return if (d > maxDist) -1 else d
    }
}

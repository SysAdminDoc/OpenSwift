package com.openswift.keyboard.engine

import java.util.Locale
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/** Indexed candidate generation and deterministic ranking for tap typing. */
class Predictor(
    private val wordList: WordList,
    private val userDict: UserDictionary
) {

    /** Returns up to [limit] suggestions, best first. */
    fun suggest(prefix: String, previousWord: String?, limit: Int = 3): List<String> {
        if (limit <= 0) return emptyList()
        val normalizedPrefix = prefix.trim().lowercase(Locale.ROOT)
        if (normalizedPrefix.isEmpty()) {
            val byBigram = previousWord?.let { userDict.nextAfter(it, limit) }.orEmpty()
            return if (byBigram.isNotEmpty()) byBigram else wordList.words.take(limit)
        }

        val budget = editBudget(normalizedPrefix.length)
        val candidates = LinkedHashSet<String>()
        candidates.addAll(wordList.prefixMatches(normalizedPrefix))
        candidates.addAll(userDict.wordsStartingWith(normalizedPrefix))
        previousWord?.let { previous ->
            userDict.nextAfter(previous, BIGRAM_CANDIDATE_LIMIT)
                .filterTo(candidates) { it.startsWith(normalizedPrefix) }
        }

        // Prefix matches dominate normal typing. Only pay for fuzzy generation when
        // there are too few direct candidates to fill a useful suggestion strip.
        if (candidates.size < max(MIN_FUZZY_POOL, limit * 4)) {
            wordList.wordsWithinLength(normalizedPrefix.length, budget)
                .filterTo(candidates) { correctionDistance(normalizedPrefix, it, budget) >= 0 }
            userDict.knownWords()
                .asSequence()
                .filter { abs(it.length - normalizedPrefix.length) <= budget }
                .filterTo(candidates) { correctionDistance(normalizedPrefix, it, budget) >= 0 }
        }

        return candidates
            .mapNotNull { word ->
                scoreCandidate(normalizedPrefix, word, previousWord, budget)?.let { score ->
                    Candidate(word, score)
                }
            }
            .sortedWith(compareByDescending<Candidate> { it.score }.thenBy { it.word })
            .take(limit)
            .map { it.word }
    }

    /** Returns a conservative correction, leaving uncertain or known words unchanged. */
    fun autoCorrect(typed: String, previousWord: String?): String {
        val normalizedTyped = typed.trim().lowercase(Locale.ROOT)
        if (normalizedTyped.length < MIN_AUTOCORRECT_LENGTH) return typed
        if (wordList.contains(normalizedTyped) || userDict.isKnown(normalizedTyped)) return typed

        val budget = editBudget(normalizedTyped.length)
        val candidates = LinkedHashSet<String>()
        wordList.wordsWithinLength(normalizedTyped.length, budget).forEach(candidates::add)
        userDict.knownWords()
            .filterTo(candidates) { abs(it.length - normalizedTyped.length) <= budget }

        val best = candidates
            .mapNotNull { word ->
                val distance = correctionDistance(normalizedTyped, word, budget)
                if (distance < 0 || !isConfidentCorrection(normalizedTyped, word, distance)) {
                    null
                } else {
                    val frequency = wordList.frequency(word) + userDict.unigramCount(word)
                    val bigram = previousWord?.let { userDict.bigramCount(it, word) } ?: 0
                    val score = ((budget - distance) * 2.0) +
                        log10(frequency.coerceAtLeast(1).toDouble() + 1.0) +
                        bigramBoost(bigram)
                    Correction(word, distance, score)
                }
            }
            .sortedWith(
                compareBy<Correction> { it.distance }
                    .thenByDescending { it.score }
                    .thenBy { it.word }
            )
            .firstOrNull()

        return best?.word ?: typed
    }

    private fun scoreCandidate(
        prefix: String,
        word: String,
        previousWord: String?,
        budget: Int
    ): Double? {
        val frequency = wordList.frequency(word) + userDict.unigramCount(word)
        val frequencyScore = log10(frequency.coerceAtLeast(1).toDouble() + 1.0)
        val bigram = previousWord?.let { userDict.bigramCount(it, word) } ?: 0

        if (word.startsWith(prefix)) {
            val completionPenalty = (word.length - prefix.length).coerceAtMost(12) * 0.03
            return PREFIX_BASE_SCORE + frequencyScore - completionPenalty + bigramBoost(bigram)
        }

        val distance = correctionDistance(prefix, word, budget)
        if (distance < 0) return null
        return (budget - distance).toDouble() + (frequencyScore * 0.5) + bigramBoost(bigram)
    }

    private fun correctionDistance(typed: String, candidate: String, budget: Int): Int {
        if (abs(typed.length - candidate.length) > budget) return -1
        return damerauLevenshtein(typed, candidate, budget)
    }

    private fun isConfidentCorrection(typed: String, candidate: String, distance: Int): Boolean {
        val longestLength = max(typed.length, candidate.length)
        return distance > 0 && distance.toDouble() / longestLength <= MAX_CORRECTION_RATIO
    }

    /** Bounded Damerau-Levenshtein distance; returns -1 when [maxDistance] is exceeded. */
    internal fun damerauLevenshtein(a: String, b: String, maxDistance: Int): Int {
        if (abs(a.length - b.length) > maxDistance) return -1
        val previousPrevious = IntArray(b.length + 1)
        var previous = IntArray(b.length + 1) { it }
        var current = IntArray(b.length + 1)

        for (i in 1..a.length) {
            current[0] = i
            var rowMinimum = current[0]
            for (j in 1..b.length) {
                val substitutionCost = if (a[i - 1] == b[j - 1]) 0 else 1
                var value = min(
                    min(current[j - 1] + 1, previous[j] + 1),
                    previous[j - 1] + substitutionCost
                )
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    value = min(value, previousPrevious[j - 2] + 1)
                }
                current[j] = value
                rowMinimum = min(rowMinimum, value)
            }
            if (rowMinimum > maxDistance) return -1
            System.arraycopy(previous, 0, previousPrevious, 0, previous.size)
            val swap = previous
            previous = current
            current = swap
        }

        return previous[b.length].takeIf { it <= maxDistance } ?: -1
    }

    private fun editBudget(length: Int): Int = when {
        length <= 4 -> 1
        length <= 8 -> 2
        else -> 3
    }

    private fun bigramBoost(count: Int): Double =
        if (count <= 0) 0.0 else BIGRAM_WEIGHT * log10(count.toDouble() + 1.0)

    private data class Candidate(val word: String, val score: Double)
    private data class Correction(val word: String, val distance: Int, val score: Double)

    private companion object {
        const val MIN_AUTOCORRECT_LENGTH = 3
        const val MIN_FUZZY_POOL = 12
        const val BIGRAM_CANDIDATE_LIMIT = 32
        const val PREFIX_BASE_SCORE = 5.0
        const val BIGRAM_WEIGHT = 5.0
        const val MAX_CORRECTION_RATIO = 0.34
    }
}

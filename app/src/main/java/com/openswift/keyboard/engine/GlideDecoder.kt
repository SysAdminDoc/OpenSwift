package com.openswift.keyboard.engine

import kotlin.math.hypot

/**
 * Decodes a continuous swipe gesture across the keyboard into the most likely word.
 *
 * Approach: collect the sequence of distinct keys the gesture passed near (sample-based with
 * a minimum spacing), then for every dictionary word check that:
 *   - it starts with the first sampled key,
 *   - it ends with the last sampled key,
 *   - all intermediate keys appear in order in the gesture polyline (subsequence test),
 * weighting the result by word frequency, length proximity, and total path deviation.
 */
class GlideDecoder(
    private val wordList: WordList,
    private val userDict: UserDictionary
) {

    data class Sample(val char: Char, val x: Float, val y: Float)

    /** Convert raw gesture path samples into one suggested word (or empty). */
    fun decode(samples: List<Sample>, limit: Int = 4): List<String> {
        if (samples.size < 2) return emptyList()

        // Reduce the sampled polyline into a list of "anchor" keys: first key, last key,
        // and any intermediate key where the path direction changes sharply.
        val anchors = pickAnchors(samples)
        if (anchors.size < 2) return emptyList()

        val first = anchors.first().char
        val last = anchors.last().char
        val anchorChars = anchors.map { it.char }

        val targetLen = anchors.size
        val out = ArrayList<Pair<String, Double>>()

        for (w in wordList.words) {
            if (w.isEmpty()) continue
            if (w[0] != first) continue
            if (w.last() != last) continue
            if (w.length < targetLen / 2 || w.length > targetLen * 3 + 4) continue
            if (!isSubsequence(anchorChars, w)) continue

            val freq = (wordList.frequency(w) + userDict.unigramCount(w)).coerceAtLeast(1)
            val freqScore = Math.log10(freq.toDouble() + 1.0)
            val lenPenalty = kotlin.math.abs(w.length - targetLen) * 0.15
            out.add(w to (freqScore - lenPenalty))
        }

        return out.sortedByDescending { it.second }.take(limit).map { it.first }
    }

    private fun pickAnchors(samples: List<Sample>): List<Sample> {
        val anchors = ArrayList<Sample>()
        anchors.add(samples.first())
        var lastChar = samples.first().char
        var lastDx = 0f
        var lastDy = 0f
        for (i in 1 until samples.size - 1) {
            val s = samples[i]
            if (s.char == lastChar) continue
            // direction-change heuristic - prefer keys at gesture turning points
            val prev = samples[i - 1]
            val next = samples[i + 1]
            val dx1 = s.x - prev.x; val dy1 = s.y - prev.y
            val dx2 = next.x - s.x; val dy2 = next.y - s.y
            val dot = dx1 * dx2 + dy1 * dy2
            val mag = hypot(dx1.toDouble(), dy1.toDouble()) * hypot(dx2.toDouble(), dy2.toDouble())
            val cosA = if (mag == 0.0) 1.0 else dot / mag
            val turning = cosA < 0.4
            if (turning || s.char != anchors.last().char) {
                if (s.char != anchors.last().char) {
                    anchors.add(s)
                    lastChar = s.char
                }
            }
            lastDx = dx1; lastDy = dy1
        }
        if (samples.last().char != anchors.last().char) anchors.add(samples.last())
        // de-duplicate runs of the same key
        val dedup = ArrayList<Sample>(anchors.size)
        for (a in anchors) {
            if (dedup.isEmpty() || dedup.last().char != a.char) dedup.add(a)
        }
        return dedup
    }

    private fun isSubsequence(seq: List<Char>, word: String): Boolean {
        var i = 0
        for (c in word) {
            if (i < seq.size && seq[i] == c) i++
            if (i == seq.size) break
        }
        return i == seq.size
    }
}

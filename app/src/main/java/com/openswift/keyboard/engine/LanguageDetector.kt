package com.openswift.keyboard.engine

import com.openswift.keyboard.data.KeyboardLanguage
import kotlin.math.ln

class LanguageDetector(
    private val languages: List<KeyboardLanguage>,
    private val wordScore: (String, String) -> Int
) {
    data class Result(
        val languageCode: String,
        val confidence: Double,
        val evidence: Int
    )

    private val accentHints = mapOf(
        "de" to setOf('ä', 'ö', 'ü', 'ß'),
        "fr" to setOf('à', 'â', 'æ', 'ç', 'é', 'è', 'ê', 'ë', 'î', 'ï', 'ô', 'œ', 'ù', 'û', 'ÿ'),
        "es" to setOf('á', 'é', 'í', 'ñ', 'ó', 'ú', 'ü', '¡', '¿'),
        "it" to setOf('à', 'è', 'é', 'ì', 'ò', 'ù')
    )

    fun detect(tokens: List<String>, currentLanguage: String): Result? {
        val normalized = tokens.mapNotNull { normalize(it) }.takeLast(MAX_TOKENS)
        if (normalized.isEmpty()) return null

        val scores = languages.associate { language ->
            language.code to scoreLanguage(language.code, normalized)
        }
        val ranked = scores.entries.sortedByDescending { it.value }
        val best = ranked.firstOrNull() ?: return null
        val runnerUp = ranked.getOrNull(1)?.value ?: 0.0
        if (best.key == currentLanguage) return null

        val evidence = normalized.count { wordScore(best.key, it) > 0 } +
            normalized.sumOf { token -> accentHints[best.key]?.count { token.contains(it) } ?: 0 }
        val confidence = best.value - runnerUp
        if (evidence < MIN_EVIDENCE || confidence < MIN_CONFIDENCE) return null
        return Result(best.key, confidence, evidence)
    }

    private fun scoreLanguage(languageCode: String, tokens: List<String>): Double {
        var score = 0.0
        val accentSet = accentHints[languageCode].orEmpty()
        for (token in tokens) {
            val frequency = wordScore(languageCode, token)
            if (frequency > 0) {
                score += 2.0 + ln(frequency.toDouble() + 1.0)
            }
            val accentMatches = accentSet.count { token.contains(it) }
            if (accentMatches > 0) {
                score += accentMatches * 8.0
            }
        }
        return score
    }

    private fun normalize(raw: String): String? {
        val normalized = raw.lowercase().filter { it.isLetter() || it == '¡' || it == '¿' }
        return normalized.takeIf { it.length >= 2 }
    }

    companion object {
        private const val MAX_TOKENS = 8
        private const val MIN_EVIDENCE = 1
        private const val MIN_CONFIDENCE = 3.0
    }
}

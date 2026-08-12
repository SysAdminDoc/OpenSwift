package com.openswift.keyboard.engine

import com.openswift.keyboard.data.KeyboardLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageDetectorTest {
    private val languages = listOf(
        KeyboardLanguage("en", "English", "en_US", "qwerty", 1),
        KeyboardLanguage("de", "German", "de_DE", "qwertz", 2),
        KeyboardLanguage("fr", "French", "fr_FR", "azerty", 3),
        KeyboardLanguage("es", "Spanish", "es_ES", "qwerty", 4),
        KeyboardLanguage("it", "Italian", "it_IT", "qwerty", 5)
    )

    private val frequencies = mapOf(
        "en" to mapOf("the" to 10000, "and" to 9000, "hello" to 3000),
        "de" to mapOf("der" to 10000, "und" to 9900, "hallo" to 3500),
        "fr" to mapOf("bonjour" to 4700, "merci" to 4600, "avec" to 6500),
        "es" to mapOf("hola" to 3400, "gracias" to 3300, "teclado" to 1800),
        "it" to mapOf("ciao" to 3400, "grazie" to 3300, "tastiera" to 1800)
    )

    private val detector = LanguageDetector(languages) { language, word ->
        frequencies[language]?.get(word) ?: 0
    }

    @Test
    fun detectsGermanFromDictionaryEvidence() {
        val result = detector.detect(listOf("und", "hallo"), currentLanguage = "en")

        assertEquals("de", result?.languageCode)
    }

    @Test
    fun detectsSpanishFromAccentEvidence() {
        val result = detector.detect(listOf("mañana"), currentLanguage = "en")

        assertEquals("es", result?.languageCode)
    }

    @Test
    fun detectsItalianFromDictionaryAndAccentEvidence() {
        val result = detector.detect(listOf("ciao", "città"), currentLanguage = "en")

        assertEquals("it", result?.languageCode)
    }

    @Test
    fun keepsCurrentLanguageWhenEvidenceIsAmbiguous() {
        val result = detector.detect(listOf("hello"), currentLanguage = "en")

        assertNull(result)
    }
}

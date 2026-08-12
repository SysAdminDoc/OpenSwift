package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardLanguagesTest {
    @Test
    fun supportedLanguagesExposeExpectedLayoutDefaults() {
        val byCode = KeyboardLanguages.all.associateBy { it.code }

        assertEquals("qwerty", byCode.getValue("en").layoutId)
        assertEquals("qwertz", byCode.getValue("de").layoutId)
        assertEquals("azerty", byCode.getValue("fr").layoutId)
        assertEquals("qwerty", byCode.getValue("es").layoutId)
        assertEquals("qwerty", byCode.getValue("it").layoutId)
    }

    @Test
    fun localeLookupFallsBackToEnglishForUnknownLocales() {
        assertEquals("de", KeyboardLanguages.byLocale("de_DE").code)
        assertEquals("fr", KeyboardLanguages.byLocale("fr-FR").code)
        assertEquals("es", KeyboardLanguages.byLocale("es_ES").code)
        assertEquals("it", KeyboardLanguages.byLocale("it_IT").code)
        assertEquals("en", KeyboardLanguages.byLocale("ja_JP").code)
        assertTrue(
            KeyboardLanguages.all.map { it.code }.containsAll(listOf("en", "de", "fr", "es", "it")),
        )
    }
}

package com.openswift.keyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlideDecoderTest {

    @Test
    fun decodesDeterministicAnchorSequence() {
        val decoder = GlideDecoder(
            WordList.fromEntries(mapOf("hello" to 1_000, "hero" to 500)),
            emptyUserDictionary()
        )

        assertEquals("hello", decoder.decode(samples("helo")).first())
    }

    @Test
    fun shiftedSamplesStillDecodeLowercaseDictionaryWords() {
        val decoder = GlideDecoder(
            WordList.fromEntries(mapOf("hello" to 1_000)),
            emptyUserDictionary()
        )

        assertEquals("hello", decoder.decode(samples("HELO")).first())
    }

    @Test
    fun learnedOnlyWordCanBeGlideCandidate() {
        val userDictionary = emptyUserDictionary().apply { repeat(3) { learn(null, "hallo") } }
        val decoder = GlideDecoder(
            WordList.fromEntries(mapOf("hero" to 1_000)),
            userDictionary
        )

        assertTrue(decoder.decode(samples("halo")).contains("hallo"))
    }

    private fun samples(chars: String): List<GlideDecoder.Sample> =
        chars.mapIndexed { index, char -> GlideDecoder.Sample(char, index.toFloat(), 0f) }
}

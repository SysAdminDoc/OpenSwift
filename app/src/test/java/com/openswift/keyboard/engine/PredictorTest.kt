package com.openswift.keyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictorTest {

    @Test
    fun prefixPredictionRanksFrequencyAndCompletionLength() {
        val predictor = predictor(
            "hello" to 1_000,
            "help" to 500,
            "helium" to 100,
            "world" to 2_000
        )

        assertEquals(listOf("hello", "help", "helium"), predictor.suggest("hel", null))
    }

    @Test
    fun learnedOnlyWordsAreFirstClassCandidates() {
        val userDictionary = emptyUserDictionary().apply {
            repeat(4) { learn(null, "helipad") }
        }
        val predictor = Predictor(
            WordList.fromEntries(mapOf("hello" to 1_000, "world" to 900)),
            userDictionary
        )

        assertTrue(predictor.suggest("heli", null).contains("helipad"))
    }

    @Test
    fun learnedBigramCanOutrankGlobalFrequency() {
        val userDictionary = emptyUserDictionary().apply {
            repeat(12) { learn("hello", "there") }
        }
        val predictor = Predictor(
            WordList.fromEntries(mapOf("the" to 100_000, "there" to 100)),
            userDictionary
        )

        assertEquals("there", predictor.suggest("th", "hello").first())
    }

    @Test
    fun autocorrectFixesCloseTyposAndPreservesUncertainInput() {
        val predictor = predictor("the" to 10_000, "keyboard" to 8_000, "swift" to 7_000)

        assertEquals("the", predictor.autoCorrect("teh", null))
        assertEquals("zzzzzz", predictor.autoCorrect("zzzzzz", null))
        assertEquals("xy", predictor.autoCorrect("xy", null))
    }

    @Test
    fun autocorrectPrefersCloserCandidateOverGlobalFrequency() {
        val predictor = predictor("cards" to 1, "caves" to 1_000_000)

        assertEquals("cards", predictor.autoCorrect("carts", null))
    }

    @Test
    fun knownUserWordIsNeverAutocorrected() {
        val userDictionary = emptyUserDictionary().apply { learn(null, "codexx") }
        val predictor = Predictor(
            WordList.fromEntries(mapOf("codex" to 1_000)),
            userDictionary
        )

        assertEquals("codexx", predictor.autoCorrect("codexx", null))
    }

    private fun predictor(vararg entries: Pair<String, Int>): Predictor =
        Predictor(WordList.fromEntries(entries.toMap()), emptyUserDictionary())
}

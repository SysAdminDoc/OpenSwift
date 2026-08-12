package com.openswift.keyboard.engine

import kotlin.system.measureTimeMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordListTest {

    @Test
    fun resourceLinesAreNormalizedAndMalformedRowsAreIgnored() {
        val parsed = WordList.parseLines(
            sequenceOf(
                " Hello\t10",
                "hello\t4",
                "world\t20",
                "missing-frequency",
                "bad\tnot-a-number"
            )
        )
        val words = WordList.fromEntries(parsed)

        assertEquals(10, words.frequency("HELLO"))
        assertEquals(listOf("world", "hello"), words.words)
    }

    @Test
    fun prefixLookupReturnsOnlyTheIndexedLexicalRange() {
        val words = WordList.fromEntries(
            mapOf("app" to 5, "apple" to 10, "apply" to 8, "banana" to 20)
        )

        assertEquals(listOf("app", "apple", "apply"), words.prefixMatches("app"))
        assertEquals(emptyList<String>(), words.prefixMatches("zzz"))
    }

    @Test
    fun largeDictionaryPrefixLookupStaysWithinBudget() {
        val entries = HashMap<String, Int>(100_100)
        repeat(100_000) { index -> entries["word%06d".format(index)] = index + 1 }
        repeat(20) { index -> entries["targetprefix%02d".format(index)] = 200_000 - index }
        val predictor = Predictor(WordList.fromEntries(entries), emptyUserDictionary())

        predictor.suggest("targetprefix", null)
        val elapsed = measureTimeMillis {
            repeat(50) { predictor.suggest("targetprefix", null) }
        }

        assertTrue("Indexed prefix lookups took ${elapsed}ms", elapsed < PREFIX_LOOKUP_BUDGET_MS)
    }

    private companion object {
        const val PREFIX_LOOKUP_BUDGET_MS = 1_000L
    }
}

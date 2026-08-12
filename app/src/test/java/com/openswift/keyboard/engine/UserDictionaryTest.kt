package com.openswift.keyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDictionaryTest {

    @Test
    fun punctuationIsRemovedBeforeLearning() {
        val dictionary = emptyUserDictionary()

        dictionary.learn("Hello!", "world,")

        assertTrue(dictionary.isKnown("world"))
        assertFalse(dictionary.knownWords().contains("world,"))
        assertEquals(1, dictionary.getWordCount())
        assertEquals(listOf("world"), dictionary.nextAfter("hello"))
    }

    @Test
    fun invalidTokensDoNotPoisonDictionary() {
        val dictionary = emptyUserDictionary()

        dictionary.learn(null, "two words")
        dictionary.learn(null, "1234")
        dictionary.learn(null, "---")

        assertEquals(0, dictionary.getWordCount())
    }

    @Test
    fun unigramsAndBigramsPersistAcrossInstances() {
        val storage = MemoryUserDictionaryStorage()
        UserDictionary(storage).apply {
            learn("hello", "world")
            learn("hello", "world")
            save()
        }

        val restored = UserDictionary(storage)

        assertEquals(2, restored.unigramCount("world"))
        assertEquals(2, restored.bigramCount("hello", "world"))
        assertEquals(listOf("world"), restored.nextAfter("hello"))
    }
}

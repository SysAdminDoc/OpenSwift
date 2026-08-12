package com.openswift.keyboard.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextTokenPolicyTest {

    @Test
    fun lettersAndDigitsRemainInCurrentToken() {
        assertTrue(TextTokenPolicy.continuesWord('a', ""))
        assertTrue(TextTokenPolicy.continuesWord('7', "abc"))
    }

    @Test
    fun connectorsRequireAnExistingToken() {
        assertFalse(TextTokenPolicy.continuesWord('\'', ""))
        assertTrue(TextTokenPolicy.continuesWord('\'', "can"))
        assertTrue(TextTokenPolicy.continuesWord('-', "privacy"))
    }

    @Test
    fun punctuationEndsPredictionToken() {
        assertFalse(TextTokenPolicy.continuesWord(',', "hello"))
        assertFalse(TextTokenPolicy.continuesWord('@', "hello"))
        assertFalse(TextTokenPolicy.continuesWord('_', "hello"))
    }
}

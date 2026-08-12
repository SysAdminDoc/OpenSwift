package com.openswift.keyboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordCommitPolicyTest {

    @Test
    fun unchangedTypedWordIsNotCommittedTwice() {
        val plan = WordCommitPolicy.plan("hello", "hello", capitalize = false)

        assertEquals(0, plan.charactersToDelete)
        assertNull(plan.textToCommit)
    }

    @Test
    fun completionReplacesAlreadyCommittedPrefix() {
        val plan = WordCommitPolicy.plan("hel", "hello", capitalize = false)

        assertEquals(3, plan.charactersToDelete)
        assertEquals("hello", plan.textToCommit)
    }

    @Test
    fun correctionPreservesInitialCapitalization() {
        val plan = WordCommitPolicy.plan("teh", "the", capitalize = true)

        assertEquals(3, plan.charactersToDelete)
        assertEquals("The", plan.textToCommit)
    }

    @Test
    fun glideWordCommitsWithoutDeletingEditorText() {
        val plan = WordCommitPolicy.plan("", "swift", capitalize = false)

        assertEquals(0, plan.charactersToDelete)
        assertEquals("swift", plan.textToCommit)
    }
}

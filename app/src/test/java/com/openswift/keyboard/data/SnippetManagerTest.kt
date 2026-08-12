package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnippetManagerTest {

    @Test
    fun createEditDeleteAndPersistenceRoundTrip() {
        val storage = MemorySnippetStorage()
        val manager = SnippetManager(storage)

        assertNull(manager.save(null, "brb", "be right back"))
        assertNull(manager.save("brb", "idk", "I don't know"))

        val restored = SnippetManager(storage)
        assertNull(restored.expand("brb"))
        assertEquals("I don't know", restored.expand("IDK"))
        assertTrue(restored.remove("idk"))
        assertTrue(SnippetManager(storage).getAll().isEmpty())
    }

    @Test
    fun validationRejectsBlankOversizedWhitespaceAndDuplicateTriggers() {
        val manager = SnippetManager(MemorySnippetStorage())

        assertNotNull(manager.save(null, "", "text"))
        assertNotNull(manager.save(null, "has space", "text"))
        assertNotNull(manager.save(null, "x".repeat(SnippetManager.MAX_TRIGGER_LENGTH + 1), "text"))
        assertNotNull(manager.save(null, "ok", ""))
        assertNull(manager.save(null, "sig", "first"))
        assertEquals("That trigger already exists.", manager.save(null, "SIG", "second"))
    }

    @Test
    fun matchingUsesLongestCaseInsensitiveTokenSuffix() {
        val manager = SnippetManager(MemorySnippetStorage()).apply {
            save(null, "sig", "short")
            save(null, ";sig", "long")
            save(null, "brb", "be right back")
        }

        assertEquals("long", manager.matchEnding("hello;SIG")?.expansion)
        assertEquals("be right back", manager.matchEnding("hello;brb")?.expansion)
        assertNull(manager.matchEnding("foobarbrb"))
    }

    @Test
    fun privacyPolicyBlocksOtherwiseMatchingExpansion() {
        val manager = SnippetManager(MemorySnippetStorage()).apply {
            save(null, "addr", "123 Main Street")
        }

        assertNotNull(SnippetExpansionPolicy.match(manager, "addr", privateField = false))
        assertNull(SnippetExpansionPolicy.match(manager, "addr", privateField = true))
    }

    @Test
    fun reloadObservesChangesFromAnotherManagerAndIgnoresCorruption() {
        val storage = MemorySnippetStorage()
        val imeManager = SnippetManager(storage)
        SnippetManager(storage).save(null, "omw", "on my way")

        assertNull(imeManager.expand("omw"))
        imeManager.reload()
        assertEquals("on my way", imeManager.expand("omw"))

        storage.value = "not-json"
        imeManager.reload()
        assertTrue(imeManager.getAll().isEmpty())
        assertFalse(imeManager.remove("missing"))
    }

    private class MemorySnippetStorage(
        var value: String = "[]"
    ) : SnippetStorage {
        override fun read(): String = value

        override fun write(value: String) {
            this.value = value
        }
    }
}

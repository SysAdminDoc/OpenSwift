package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardHistoryTest {

    @Test
    fun captureRequiresOptInAndNonPrivateNonSensitiveText() {
        assertFalse(ClipboardHistory.shouldCapture(false, false, false, "copy"))
        assertFalse(ClipboardHistory.shouldCapture(true, true, false, "copy"))
        assertFalse(ClipboardHistory.shouldCapture(true, false, true, "copy"))
        assertFalse(ClipboardHistory.shouldCapture(true, false, false, "  "))
        assertTrue(ClipboardHistory.shouldCapture(true, false, false, "copy"))
    }

    @Test
    fun emptyAndDuplicateValuesAreIgnored() {
        val current = listOf("newest", "older")

        assertSame(current, ClipboardHistory.withCapturedItem(current, ""))
        assertSame(current, ClipboardHistory.withCapturedItem(current, "older"))
    }

    @Test
    fun newItemsAreNewestFirstAndRetentionIsBounded() {
        val current = (1..ClipboardHistory.MAX_ITEMS).map { "item-$it" }

        val updated = ClipboardHistory.withCapturedItem(current, "latest")

        assertEquals(ClipboardHistory.MAX_ITEMS, updated.size)
        assertEquals("latest", updated.first())
        assertEquals("item-${ClipboardHistory.MAX_ITEMS - 1}", updated.last())
    }
}

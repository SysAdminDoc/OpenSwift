package com.openswift.keyboard.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiCatalogTest {
    @Test
    fun categoriesIncludeRecentsFavoritesAndSearchableGroups() {
        assertEquals(EmojiCatalog.RECENTS, EmojiCatalog.categories.first())
        assertTrue(EmojiCatalog.categories.contains(EmojiCatalog.FAVORITES))
        assertTrue(EmojiCatalog.categories.containsAll(listOf("Smile", "Food", "Nature", "Travel", "Symbol")))
    }

    @Test
    fun searchMatchesKeywordsAcrossCategories() {
        val heart = EmojiCatalog.search("heart").map { it.value }
        val travel = EmojiCatalog.search("rocket").map { it.value }

        assertTrue("❤️" in heart)
        assertTrue("🚀" in travel)
    }

    @Test
    fun everyEntryIsAddressableByValue() {
        assertEquals(EmojiCatalog.entries.size, EmojiCatalog.byValue.size)
    }
}

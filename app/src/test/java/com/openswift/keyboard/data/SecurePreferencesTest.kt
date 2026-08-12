package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurePreferencesTest {

    @Test
    fun migrationCopiesSupportedValuesAndRemovesPlaintextKeys() {
        val legacy = mapOf<String, Any>(
            "text" to "private text",
            "count" to 3,
            "enabled" to true,
            "labels" to setOf("one", "two")
        )

        val plan = SecurePreferences.migrationPlan(legacy, encryptedKeys = emptySet())

        assertEquals(legacy, plan.entriesToWrite)
        assertEquals(legacy.keys, plan.legacyKeysToRemove)
    }

    @Test
    fun migrationPreservesEncryptedValuesAndStillRemovesLegacyCopy() {
        val plan = SecurePreferences.migrationPlan(
            legacyEntries = mapOf("data" to "legacy", "new" to "value"),
            encryptedKeys = setOf("data")
        )

        assertFalse(plan.entriesToWrite.containsKey("data"))
        assertEquals("value", plan.entriesToWrite["new"])
        assertTrue(plan.legacyKeysToRemove.containsAll(listOf("data", "new")))
    }

    @Test
    fun migrationFilterLeavesUnrelatedLegacyPreferencesUntouched() {
        val plan = SecurePreferences.migrationPlan(
            legacyEntries = mapOf("custom_dark" to "theme", "unrelated" to "setting"),
            encryptedKeys = emptySet(),
            migrateKey = { it.startsWith("custom_") }
        )

        assertEquals(mapOf("custom_dark" to "theme"), plan.entriesToWrite)
        assertEquals(setOf("custom_dark"), plan.legacyKeysToRemove)
    }
}

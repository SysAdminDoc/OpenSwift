package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTest {

    @Test
    fun defaultsMatchKeyboardBehavior() {
        val settings = Settings(MutableMapSettingsStore())

        assertEquals("amoled", settings.theme)
        assertEquals("qwerty", settings.layout)
        assertEquals("en", settings.language)
        assertTrue(settings.glideEnabled)
        assertTrue(settings.autoCorrect)
        assertFalse(settings.clipboardEnabled)
    }

    @Test
    fun valuesPersistAcrossSettingsInstances() {
        val store = MutableMapSettingsStore()
        Settings(store).apply {
            theme = "high_contrast"
            glideEnabled = false
            keyHeightDp = 64
            clipboardEnabled = true
        }

        val restored = Settings(store)

        assertEquals("high_contrast", restored.theme)
        assertFalse(restored.glideEnabled)
        assertEquals(64, restored.keyHeightDp)
        assertTrue(restored.clipboardEnabled)
    }

    @Test
    fun changingLanguagePersistsItsDefaultLayout() {
        val store = MutableMapSettingsStore()
        val settings = Settings(store)

        settings.language = "de"

        assertEquals("de", Settings(store).language)
        assertEquals("qwertz", Settings(store).layout)
    }
}

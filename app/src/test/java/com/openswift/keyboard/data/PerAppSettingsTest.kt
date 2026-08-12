package com.openswift.keyboard.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerAppSettingsTest {

    @Test
    fun profilePersistsPredictionGlideAndHeightOverrides() {
        val store = MutableMapPerAppSettingsStore()
        val settings = PerAppSettings(store)

        assertNull(
            settings.saveAppConfig(
                PerAppSettings.AppConfig(
                    packageName = "com.example.mail",
                    predictionsDisabled = true,
                    glideDisabled = true,
                    keyHeightOverride = 64,
                ),
            ),
        )

        assertEquals(
            PerAppSettings.AppConfig("com.example.mail", true, true, 64),
            PerAppSettings(store).getAppConfig("com.example.mail"),
        )
        assertEquals(listOf("com.example.mail"), settings.getAllAppConfigs().map { it.packageName })
    }

    @Test
    fun effectiveProfileInheritsGlobalValuesAndOnlyDisablesFeatures() {
        val inherited = PerAppSettings.resolve(
            PerAppSettings.AppConfig("com.example.notes"),
            globalGlideEnabled = true,
            globalKeyHeightDp = 56,
        )
        val overridden = PerAppSettings.resolve(
            PerAppSettings.AppConfig(
                "com.example.game",
                predictionsDisabled = true,
                glideDisabled = false,
                keyHeightOverride = 48,
            ),
            globalGlideEnabled = false,
            globalKeyHeightDp = 56,
        )

        assertTrue(inherited.predictionsEnabled)
        assertTrue(inherited.glideEnabled)
        assertEquals(56, inherited.keyHeightDp)
        assertFalse(overridden.predictionsEnabled)
        assertFalse(overridden.glideEnabled)
        assertEquals(48, overridden.keyHeightDp)
    }

    @Test
    fun savingAnEmptyProfileRemovesEveryOverride() {
        val store = MutableMapPerAppSettingsStore()
        val settings = PerAppSettings(store)
        settings.setAppConfig(
            packageName = "com.example.editor",
            glideDisabled = true,
            keyHeight = 60,
            predictionsDisabled = true,
        )

        assertNull(settings.saveAppConfig(PerAppSettings.AppConfig("com.example.editor")))

        assertFalse(settings.getAppConfig("com.example.editor").hasOverrides)
        assertTrue(settings.getAllAppConfigs().isEmpty())
        assertTrue(store.keys.isEmpty())
    }

    @Test
    fun resetAllRemovesAllProfiles() {
        val settings = PerAppSettings(MutableMapPerAppSettingsStore())
        settings.setAppConfig("com.example.one", glideDisabled = true)
        settings.setAppConfig("com.example.two", predictionsDisabled = true)

        settings.resetAll()

        assertTrue(settings.getAllAppConfigs().isEmpty())
    }

    @Test
    fun packageAndHeightValidationReturnUserFacingErrors() {
        val settings = PerAppSettings(MutableMapPerAppSettingsStore())

        assertEquals(
            "Enter a valid app package name, such as com.example.app.",
            settings.saveAppConfig(PerAppSettings.AppConfig("Mail")),
        )
        assertEquals(
            "Key height must be between 48 and 72 dp.",
            settings.saveAppConfig(
                PerAppSettings.AppConfig("com.example.mail", keyHeightOverride = 80),
            ),
        )
        assertTrue(settings.getAllAppConfigs().isEmpty())
    }
}

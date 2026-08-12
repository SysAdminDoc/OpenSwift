package com.openswift.keyboard.data

import com.openswift.keyboard.layout.KeyCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomizationPackageParserTest {

    @Test
    fun parsesValidatedThemeAndLayoutPackage() {
        val parsed = CustomizationPackageParser.parse(validPackage())

        assertEquals("Colemak Night", parsed.name)
        assertEquals(1, parsed.themes.size)
        assertEquals("custom_midnight", parsed.themes.single().id)
        assertEquals(0xFF10131A.toInt(), parsed.themes.single().background)
        assertEquals(1, parsed.layouts.size)
        assertEquals("custom_layout_colemak", parsed.layouts.single().id)
        assertEquals(4, parsed.layouts.single().layout.rows.size)
        assertEquals(
            1,
            parsed.layouts.single().layout.rows.flatten().count { it.code == KeyCode.SPACE },
        )
    }

    @Test
    fun storedLayoutRoundTripsWithoutLosingKeyMetadata() {
        val layout = CustomizationPackageParser.parse(validPackage()).layouts.single()

        val restored = CustomizationPackageParser.decodeLayout(
            CustomizationPackageParser.encodeLayout(layout),
        )

        assertEquals(layout, restored)
    }

    @Test
    fun invalidJsonHasAUserFacingError() {
        val error = packageError("not json")

        assertEquals("This file is not valid JSON.", error)
    }

    @Test
    fun invalidColorIdentifiesTheExactField() {
        val error = packageError(validPackage().replace("#FF10131A", "navy"))

        assertEquals(
            "themes[0].colors.background must be #RRGGBB or #AARRGGBB.",
            error,
        )
    }

    @Test
    fun layoutsMustRetainEssentialKeyboardActions() {
        val withoutDelete = validPackage().replace(
            "{\"label\":\"⌫\",\"action\":\"delete\",\"width\":1.5}",
            "{\"label\":\"-\"}",
        )

        val error = packageError(withoutDelete)

        assertEquals(
            "layouts[0] must contain exactly one key with action \"delete\".",
            error,
        )
    }

    @Test
    fun duplicateCharacterKeysAreRejected() {
        val duplicate = validPackage().replace("{\"label\":\"p\"}", "{\"label\":\"q\"}")

        val error = packageError(duplicate)

        assertTrue(error.contains("duplicates character key \"q\""))
    }

    @Test
    fun packageRequiresAtLeastOneCustomization() {
        val error = packageError(
            """{"format":"openswift.customization","schemaVersion":1,"name":"Empty"}""",
        )

        assertEquals("The package must contain at least one theme or layout.", error)
    }

    private fun packageError(raw: String): String = try {
        CustomizationPackageParser.parse(raw)
        error("Expected package validation to fail")
    } catch (error: CustomizationPackageException) {
        error.message.orEmpty()
    }

    private fun validPackage(): String =
        """
        {
          "format": "openswift.customization",
          "schemaVersion": 1,
          "name": "Colemak Night",
          "themes": [
            {
              "id": "custom_midnight",
              "name": "Midnight",
              "colors": {
                "background": "#FF10131A",
                "keyBackground": "#FF202633",
                "keyModifierBackground": "#FF171B24",
                "keyText": "#FFF4F7FF",
                "keyAccent": "#FF78A9FF",
                "suggestionBackground": "#FF10131A",
                "suggestionText": "#FFF4F7FF",
                "gestureTrail": "#FFC792EA"
              }
            }
          ],
          "layouts": [
            {
              "id": "custom_layout_colemak",
              "name": "Colemak",
              "rows": [
                [
                  {"label":"q"},{"label":"w"},{"label":"f"},{"label":"p"},{"label":"g"},
                  {"label":"j"},{"label":"l"},{"label":"u"},{"label":"y"},{"label":";"}
                ],
                [
                  {"label":"a","popup":["á","à"]},{"label":"r"},{"label":"s"},
                  {"label":"t"},{"label":"d"},{"label":"h"},{"label":"n"},
                  {"label":"e"},{"label":"i"},{"label":"o"}
                ],
                [
                  {"label":"⇧","action":"shift","width":1.5},{"label":"z"},{"label":"x"},
                  {"label":"c"},{"label":"v"},{"label":"b"},{"label":"k"},{"label":"m"},
                  {"label":"⌫","action":"delete","width":1.5}
                ],
                [
                  {"label":"?123","action":"symbols","width":1.5},
                  {"label":",","action":"comma"},
                  {"label":"space","action":"space","width":4},
                  {"label":".","action":"period"},
                  {"label":"⏎","action":"enter","width":1.5}
                ]
              ]
            }
          ]
        }
        """.trimIndent()
}

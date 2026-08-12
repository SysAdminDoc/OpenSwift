package com.openswift.keyboard.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class DataPortabilityTest {

    @Test
    fun mergeDictionaryJsonSumsUnigramsAndBigrams() {
        val current = JSONObject(
            """
            {
              "u": {"hello": 2, "swift": 1},
              "b": {"hello": {"world": 3}}
            }
            """.trimIndent()
        )
        val imported = JSONObject(
            """
            {
              "u": {"hello": 5, "keyboard": 4},
              "b": {"hello": {"world": 2, "there": 1}, "swift": {"key": 1}}
            }
            """.trimIndent()
        )

        val merged = DataPortability.mergeDictionaryJson(current, imported)

        assertEquals(7, merged.getJSONObject("u").getInt("hello"))
        assertEquals(1, merged.getJSONObject("u").getInt("swift"))
        assertEquals(4, merged.getJSONObject("u").getInt("keyboard"))
        assertEquals(5, merged.getJSONObject("b").getJSONObject("hello").getInt("world"))
        assertEquals(1, merged.getJSONObject("b").getJSONObject("hello").getInt("there"))
        assertEquals(1, merged.getJSONObject("b").getJSONObject("swift").getInt("key"))
    }

    @Test
    fun mergeSnippetJsonKeepsOrderAndImportedTriggerWins() {
        val current = JSONArray(
            """
            [
              {"t": "brb", "x": "be right back"},
              {"t": "sig", "x": "old"}
            ]
            """.trimIndent()
        )
        val imported = JSONArray(
            """
            [
              {"t": "sig", "x": "new"},
              {"t": "addr", "x": "123 Main"}
            ]
            """.trimIndent()
        )

        val merged = DataPortability.mergeSnippetJson(current, imported)

        assertEquals("brb", merged.getJSONObject(0).getString("t"))
        assertEquals("sig", merged.getJSONObject(1).getString("t"))
        assertEquals("new", merged.getJSONObject(1).getString("x"))
        assertEquals("addr", merged.getJSONObject(2).getString("t"))
    }

    @Test
    fun mergeThemeJsonUsesThemeIdAsOverwriteKey() {
        val current = JSONArray("""[{"id":"custom_1","name":"Old"},{"id":"custom_2","name":"Keep"}]""")
        val imported = JSONArray("""[{"id":"custom_1","name":"New"}]""")

        val merged = DataPortability.mergeThemeJson(current, imported)

        assertEquals(2, merged.length())
        assertEquals("New", merged.getJSONObject(0).getString("name"))
        assertEquals("Keep", merged.getJSONObject(1).getString("name"))
    }

    @Test
    fun fullBackupValidatesAndIncludesCustomLayouts() {
        val parsed = DataPortability.parseImport(
            validDocument(includeLayouts = true).toString(),
            DataPortability.Scope.FULL_BACKUP,
        )

        assertEquals(1, parsed.userDictionaries.getJSONObject("en").getJSONObject("u").length())
        assertEquals(1, parsed.snippets.length())
        assertEquals(1, parsed.customThemes.length())
        assertEquals("custom_layout_colemak", parsed.customLayouts.getJSONObject(0).getString("id"))
    }

    @Test
    fun olderFullBackupWithoutLayoutsRemainsImportable() {
        val parsed = DataPortability.parseImport(
            validDocument(includeLayouts = false).toString(),
            DataPortability.Scope.FULL_BACKUP,
        )

        assertEquals(0, parsed.customLayouts.length())
    }

    @Test
    fun encryptedSyncRejectsLayoutData() {
        val error = assertThrows(DataPortabilityException::class.java) {
            DataPortability.parseImport(
                validDocument(includeLayouts = true).toString(),
                DataPortability.Scope.ENCRYPTED_SYNC,
            )
        }

        assertTrue(error.message.orEmpty().contains("cannot contain custom layouts"))
    }

    @Test
    fun nestedTypedDataMustPassValidationBeforeImport() {
        val document = validDocument(includeLayouts = false)
        document.getJSONObject("userDictionaries")
            .getJSONObject("en")
            .getJSONObject("u")
            .put("Not Normalized!", 0)

        val error = assertThrows(DataPortabilityException::class.java) {
            DataPortability.parseImport(document.toString(), DataPortability.Scope.FULL_BACKUP)
        }

        assertTrue(error.message.orEmpty().contains("invalid or non-normalized word"))
    }

    @Test
    fun invalidSnippetAndThemeShapesAreRejected() {
        val invalidSnippet = validDocument(includeLayouts = false).also { document ->
            document.getJSONArray("snippets").getJSONObject(0).put("t", "two words")
        }
        val snippetError = assertThrows(DataPortabilityException::class.java) {
            DataPortability.parseImport(invalidSnippet.toString(), DataPortability.Scope.FULL_BACKUP)
        }
        assertTrue(snippetError.message.orEmpty().contains("Trigger cannot contain spaces"))

        val invalidTheme = validDocument(includeLayouts = false).also { document ->
            document.getJSONArray("customThemes").getJSONObject(0).put("bg", "black")
        }
        val themeError = assertThrows(DataPortabilityException::class.java) {
            DataPortability.parseImport(invalidTheme.toString(), DataPortability.Scope.FULL_BACKUP)
        }
        assertTrue(themeError.message.orEmpty().contains("customThemes[0].bg must be an integer"))
    }

    @Test
    fun layoutMergeKeepsOrderAndImportedIdWins() {
        val current = JSONArray().put(validLayout().put("name", "Old"))
        val imported = JSONArray()
            .put(validLayout().put("name", "New"))
            .put(validLayout().put("id", "custom_layout_second").put("name", "Second"))

        val merged = DataPortability.mergeLayoutJson(current, imported)

        assertEquals(2, merged.length())
        assertEquals("New", merged.getJSONObject(0).getString("name"))
        assertEquals("custom_layout_second", merged.getJSONObject(1).getString("id"))
    }

    private fun validDocument(includeLayouts: Boolean): JSONObject = JSONObject()
        .put("schemaVersion", 1)
        .put(
            "userDictionaries",
            JSONObject().put(
                "en",
                JSONObject()
                    .put("u", JSONObject().put("hello", 2))
                    .put("b", JSONObject().put("hello", JSONObject().put("world", 1))),
            ),
        )
        .put("snippets", JSONArray().put(JSONObject().put("t", "brb").put("x", "be right back")))
        .put(
            "customThemes",
            JSONArray().put(
                JSONObject()
                    .put("id", "custom_midnight")
                    .put("name", "Midnight")
                    .put("bg", 0xFF10131A.toInt())
                    .put("kbg", 0xFF202633.toInt())
                    .put("kmbg", 0xFF171B24.toInt())
                    .put("kt", 0xFFF4F7FF.toInt())
                    .put("ka", 0xFF78A9FF.toInt())
                    .put("sbg", 0xFF10131A.toInt())
                    .put("st", 0xFFF4F7FF.toInt())
                    .put("gt", 0xFFC792EA.toInt()),
            ),
        )
        .also { document ->
            if (includeLayouts) document.put("customLayouts", JSONArray().put(validLayout()))
        }

    private fun validLayout(): JSONObject = JSONObject(
        """
        {
          "id":"custom_layout_colemak",
          "name":"Colemak",
          "rows":[
            [
              {"label":"q"},{"label":"w"},{"label":"f"},{"label":"p"},{"label":"g"},
              {"label":"j"},{"label":"l"},{"label":"u"},{"label":"y"},{"label":";"}
            ],
            [
              {"label":"a"},{"label":"r"},{"label":"s"},{"label":"t"},{"label":"d"},
              {"label":"h"},{"label":"n"},{"label":"e"},{"label":"i"},{"label":"o"}
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
        """.trimIndent(),
    )
}

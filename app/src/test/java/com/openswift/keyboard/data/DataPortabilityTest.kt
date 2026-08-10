package com.openswift.keyboard.data

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
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
}

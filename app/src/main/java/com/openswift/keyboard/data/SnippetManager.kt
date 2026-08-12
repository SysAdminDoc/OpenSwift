package com.openswift.keyboard.data

/**
 * Snippet/phrase manager: stores user-defined text shortcuts.
 * Loaded from SharedPreferences as JSON array.
 *
 * Usage: long-press a key to insert snippet, or access via settings.
 */
class SnippetManager(ctx: android.content.Context) {

    private val prefs = SecurePreferences.open(ctx, TypedDataStores.SNIPPETS)
    private val snippets: MutableList<Snippet> = mutableListOf()

    data class Snippet(val trigger: String, val text: String)

    init { load() }

    fun add(trigger: String, text: String) {
        snippets.add(Snippet(trigger, text))
        save()
    }

    fun remove(trigger: String) {
        snippets.removeAll { it.trigger == trigger }
        save()
    }

    fun expand(trigger: String): String? = snippets.find { it.trigger == trigger }?.text

    fun getAll(): List<Snippet> = snippets.toList()

    private fun load() {
        val raw = prefs.getString("data", "[]") ?: "[]"
        runCatching {
            val arr = org.json.JSONArray(raw)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                snippets.add(Snippet(obj.getString("t"), obj.getString("x")))
            }
        }
    }

    private fun save() {
        val arr = org.json.JSONArray()
        snippets.forEach { s ->
            arr.put(org.json.JSONObject().apply {
                put("t", s.trigger)
                put("x", s.text)
            })
        }
        prefs.edit().putString("data", arr.toString()).apply()
    }
}

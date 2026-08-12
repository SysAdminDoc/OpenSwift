package com.openswift.keyboard.data

import android.content.ClipboardManager as SystemClipboard
import android.content.Context
import org.json.JSONArray

/** Tracks the most recent clipboard items for the keyboard's clipboard panel. */
class ClipboardHistory(ctx: Context) {

    private val prefs = SecurePreferences.open(ctx, TypedDataStores.CLIPBOARD_HISTORY)

    fun items(): List<String> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        val arr = JSONArray(raw)
        return List(arr.length()) { arr.getString(it) }
    }

    fun add(text: String) {
        if (text.isBlank()) return
        val current = items().toMutableList()
        current.remove(text)
        current.add(0, text)
        while (current.size > MAX_ITEMS) current.removeAt(current.size - 1)
        save(current)
    }

    fun remove(text: String) {
        save(items().filter { it != text })
    }

    fun clear() = save(emptyList())

    private fun save(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    fun captureSystem(ctx: Context) {
        val cb = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as SystemClipboard
        val clip = cb.primaryClip ?: return
        if (clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(ctx)?.toString() ?: return
        add(text)
    }

    companion object { const val MAX_ITEMS = 25 }
}

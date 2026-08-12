package com.openswift.keyboard.data

import android.content.ClipboardManager as SystemClipboard
import android.content.ClipDescription
import android.content.Context
import org.json.JSONArray

/** Tracks the most recent clipboard items for the keyboard's clipboard panel. */
class ClipboardHistory(ctx: Context) {

    private val prefs = SecurePreferences.open(ctx, TypedDataStores.CLIPBOARD_HISTORY)

    fun items(): List<String> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.getString(it) }
        }.getOrDefault(emptyList())
    }

    fun add(text: String): Boolean {
        val current = items()
        val updated = withCapturedItem(current, text)
        if (updated == current) return false
        save(updated)
        return true
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

    fun captureSystem(ctx: Context, enabled: Boolean, privateField: Boolean): Boolean {
        if (!enabled || privateField) return false
        val cb = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as SystemClipboard
        val clip = cb.primaryClip ?: return false
        if (clip.itemCount == 0 || isSensitive(clip.description)) return false
        val text = clip.getItemAt(0).coerceToText(ctx)?.toString() ?: return false
        if (!shouldCapture(enabled, privateField, sensitiveClip = false, text)) return false
        return add(text)
    }

    private fun isSensitive(description: ClipDescription): Boolean {
        val extras = description.extras ?: return false
        return extras.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) ||
            extras.getBoolean(SENSITIVE_CLIPBOARD_EXTRA, false)
    }

    companion object {
        const val MAX_ITEMS = 25
        private const val SENSITIVE_CLIPBOARD_EXTRA = "android.content.extra.IS_SENSITIVE"

        internal fun shouldCapture(
            enabled: Boolean,
            privateField: Boolean,
            sensitiveClip: Boolean,
            text: String?
        ): Boolean = enabled && !privateField && !sensitiveClip && !text.isNullOrBlank()

        internal fun withCapturedItem(current: List<String>, text: String): List<String> {
            if (text.isBlank() || text in current) return current
            return (listOf(text) + current).take(MAX_ITEMS)
        }
    }
}

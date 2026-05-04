package com.openswift.keyboard.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

/**
 * Accessibility support: announces key presses and suggestions for screen readers.
 * Integrates with Android's AccessibilityManager for TalkBack compatibility.
 */
class AccessibilityHelper(private val accessibilityManager: AccessibilityManager) {

    fun announceKey(key: String) {
        if (accessibilityManager.isEnabled) {
            val event = AccessibilityEvent.obtain()
            event.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            event.text.add("Key: $key")
            accessibilityManager.sendAccessibilityEvent(event)
        }
    }

    fun announceSuggestions(suggestions: List<String>) {
        if (accessibilityManager.isEnabled && suggestions.isNotEmpty()) {
            val event = AccessibilityEvent.obtain()
            event.eventType = AccessibilityEvent.TYPE_ANNOUNCEMENT
            event.text.add("Suggestions: ${suggestions.joinToString(", ")}")
            accessibilityManager.sendAccessibilityEvent(event)
        }
    }

    fun isScreenReaderEnabled(): Boolean = accessibilityManager.isEnabled
}

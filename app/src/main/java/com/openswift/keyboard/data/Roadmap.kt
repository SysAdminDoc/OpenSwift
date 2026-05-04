package com.openswift.keyboard.data

/** Clipboard manager integration — optionally expose clipboard history to IME via long-press. */
class ClipboardPanel {
    // Placeholder for future clipboard UI panel in keyboard
}

/**
 * Number row variant: swipe up from space bar to reveal number row.
 * Requires gesture recognition in KeyboardView.onTouchEvent.
 */
class NumberRowVariant {
    // TODO: Implement on next release
}

/**
 * Per-app settings: disable glide in games, adjust key height per app, etc.
 * Requires PackageManager integration to detect foreground app.
 */
class PerAppSettings {
    // TODO: Implement on next release
}

/**
 * Custom phrase insertion: long-press specific key to open snippet manager.
 * Store snippets in SharedPreferences as JSON array.
 */
class SnippetManager {
    // TODO: Implement on next release
}

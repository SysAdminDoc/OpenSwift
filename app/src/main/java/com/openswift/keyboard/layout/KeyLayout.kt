package com.openswift.keyboard.layout

/** Single key on the keyboard. */
data class Key(
    val label: String,
    val code: Int,                 // Unicode codepoint, or KeyCode.* for special keys
    val widthWeight: Float = 1f,
    val popup: List<String> = emptyList(),  // long-press alternates
    val isModifier: Boolean = false
)

object KeyCode {
    const val SHIFT = -1
    const val DELETE = -2
    const val ENTER = -3
    const val SPACE = -4
    const val SYMBOLS = -5
    const val LANGUAGE = -6
    const val EMOJI = -7
    const val SETTINGS = -8
    const val ABC = -9
    const val SHIFT_SYMBOLS = -10
    const val COMMA = -11
    const val PERIOD = -12
}

/** Static keyboard layout: rows of keys. */
data class KeyLayout(
    val id: String,
    val rows: List<List<Key>>
)

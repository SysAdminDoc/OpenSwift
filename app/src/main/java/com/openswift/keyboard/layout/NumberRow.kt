package com.openswift.keyboard.layout

/**
 * Number row overlay: displays "1234567890" as a swipeable row above the keyboard
 * for quick numeric entry. Shows only when explicitly requested (future: swipe-up gesture).
 */
data class NumberRow(
    val keys: List<Key> = (1..10).map { i ->
        val ch = if (i == 10) '0' else (i + 48).toChar()
        Key(ch.toString(), ch.code)
    }
)

object NumberRowLayout {
    val default = NumberRow()

    fun toLayout(): KeyLayout = KeyLayout(
        "numbers",
        listOf(default.keys)
    )
}

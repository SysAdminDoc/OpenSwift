package com.openswift.keyboard.layout

import com.openswift.keyboard.layout.KeyCode as KC

private fun letter(c: Char, popup: List<String> = emptyList()) =
    Key(c.toString(), c.code, popup = popup)

object Layouts {

    /** Long-press popup alternates (mainly accents). */
    private val popups: Map<Char, List<String>> = mapOf(
        'a' to listOf("á","à","â","ä","ã","å","æ","ā"),
        'e' to listOf("é","è","ê","ë","ē","ę"),
        'i' to listOf("í","ì","î","ï","ī"),
        'o' to listOf("ó","ò","ô","ö","õ","ø","œ","ō"),
        'u' to listOf("ú","ù","û","ü","ū"),
        'y' to listOf("ý","ÿ"),
        'n' to listOf("ñ","ń"),
        'c' to listOf("ç","ć","č"),
        's' to listOf("ß","ś","š"),
        'z' to listOf("ž","ź","ż"),
        'l' to listOf("ł"),
        'd' to listOf("đ"),
    )

    val Qwerty = KeyLayout(
        "qwerty",
        listOf(
            "qwertyuiop".map { letter(it, popups[it] ?: emptyList()) },
            buildList {
                add(Key("", KC.SPACER, widthWeight = 0.5f))
                addAll("asdfghjkl".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("", KC.SPACER, widthWeight = 0.5f))
            },
            buildList {
                add(Key("Shift", KC.SHIFT, widthWeight = 1.5f, isModifier = true))
                addAll("zxcvbnm".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("Delete", KC.DELETE, widthWeight = 1.5f, isModifier = true))
            },
            bottomRow()
        )
    )

    val Qwertz = KeyLayout(
        "qwertz",
        listOf(
            "qwertzuiop".map { letter(it, popups[it] ?: emptyList()) },
            buildList {
                add(Key("", KC.SPACER, widthWeight = 0.5f))
                addAll("asdfghjkl".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("", KC.SPACER, widthWeight = 0.5f))
            },
            buildList {
                add(Key("Shift", KC.SHIFT, widthWeight = 1.5f, isModifier = true))
                addAll("yxcvbnm".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("Delete", KC.DELETE, widthWeight = 1.5f, isModifier = true))
            },
            bottomRow()
        )
    )

    val Azerty = KeyLayout(
        "azerty",
        listOf(
            "azertyuiop".map { letter(it, popups[it] ?: emptyList()) },
            "qsdfghjklm".map { letter(it, popups[it] ?: emptyList()) },
            buildList {
                add(Key("Shift", KC.SHIFT, widthWeight = 1.5f, isModifier = true))
                addAll("wxcvbn".map { letter(it, popups[it] ?: emptyList()) })
                add(Key("Delete", KC.DELETE, widthWeight = 1.5f, isModifier = true))
            },
            bottomRow()
        )
    )

    val Symbols = KeyLayout(
        "symbols",
        listOf(
            "1234567890".map { letter(it) },
            "@#\$_&-+()/".map { letter(it) },
            buildList {
                add(Key("=\\<", KC.SHIFT_SYMBOLS, widthWeight = 1.5f, isModifier = true))
                "*\"':;!?".forEach { add(letter(it)) }
                add(Key("Delete", KC.DELETE, widthWeight = 1.5f, isModifier = true))
            },
            bottomRow(symbols = true)
        )
    )

    val SymbolsShift = KeyLayout(
        "symbols2",
        listOf(
            "~`|•√π÷×§∆".map { letter(it) },
            "£¥€°^{}\\©®".map { letter(it) },
            buildList {
                add(Key("?123", KC.SHIFT_SYMBOLS, widthWeight = 1.5f, isModifier = true))
                "%©®™✓[]".forEach { add(letter(it)) }
                add(Key("Delete", KC.DELETE, widthWeight = 1.5f, isModifier = true))
            },
            bottomRow(symbols = true)
        )
    )

    private fun bottomRow(symbols: Boolean = false): List<Key> = listOf(
        Key(if (symbols) "ABC" else "?123", if (symbols) KC.ABC else KC.SYMBOLS, widthWeight = 1.4f, isModifier = true),
        Key("Emoji", KC.EMOJI, isModifier = true),
        Key("Clipboard", KC.CLIPBOARD, isModifier = true),
        Key(",", KC.COMMA, widthWeight = 0.8f, popup = listOf("!", "?", ";", ":")),
        Key("space", KC.SPACE, widthWeight = 4f),
        Key(".", KC.PERIOD, widthWeight = 0.8f, popup = listOf("…", "!", "?")),
        Key("Settings", KC.SETTINGS, isModifier = true),
        Key("Enter", KC.ENTER, widthWeight = 1.4f, isModifier = true)
    )

    fun byId(id: String): KeyLayout = when (id) {
        "qwertz" -> Qwertz
        "azerty" -> Azerty
        else -> Qwerty
    }
}

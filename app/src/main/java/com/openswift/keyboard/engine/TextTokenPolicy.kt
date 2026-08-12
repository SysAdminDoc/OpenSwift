package com.openswift.keyboard.engine

/** Defines which committed characters may remain in prediction/learning tokens. */
internal object TextTokenPolicy {
    private val connectors = setOf('\'', '’', '-')

    fun continuesWord(character: Char, currentWord: CharSequence): Boolean =
        character.isLetterOrDigit() || (currentWord.isNotEmpty() && character in connectors)
}

package com.openswift.keyboard.engine

/** Pure replacement plan for words whose prefix has already been committed to the editor. */
internal object WordCommitPolicy {
    data class Plan(
        val charactersToDelete: Int,
        val textToCommit: String?
    )

    fun plan(typedWord: String, selectedWord: String, capitalize: Boolean): Plan {
        val renderedWord = if (capitalize && typedWord.isNotEmpty()) {
            selectedWord.replaceFirstChar { it.uppercase() }
        } else {
            selectedWord
        }
        return when {
            typedWord.isEmpty() -> Plan(0, renderedWord)
            typedWord.equals(selectedWord, ignoreCase = true) -> Plan(0, null)
            else -> Plan(typedWord.length, renderedWord)
        }
    }
}

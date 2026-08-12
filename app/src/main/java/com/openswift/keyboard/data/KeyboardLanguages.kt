package com.openswift.keyboard.data

import com.openswift.keyboard.R

data class KeyboardLanguage(
    val code: String,
    val name: String,
    val locale: String,
    val layoutId: String,
    val wordListRes: Int
)

object KeyboardLanguages {
    val English = KeyboardLanguage("en", "English", "en_US", "qwerty", R.raw.words)

    val all = listOf(
        English,
        KeyboardLanguage("de", "German", "de_DE", "qwertz", R.raw.words_de),
        KeyboardLanguage("fr", "French", "fr_FR", "azerty", R.raw.words_fr),
        KeyboardLanguage("es", "Spanish", "es_ES", "qwerty", R.raw.words_es),
        KeyboardLanguage("it", "Italian", "it_IT", "qwerty", R.raw.words_it)
    )

    fun byCode(code: String?): KeyboardLanguage {
        val normalized = code.orEmpty().lowercase().substringBefore('_').substringBefore('-')
        return all.firstOrNull { it.code == normalized } ?: English
    }

    fun byLocale(locale: String?): KeyboardLanguage = byCode(locale)
}

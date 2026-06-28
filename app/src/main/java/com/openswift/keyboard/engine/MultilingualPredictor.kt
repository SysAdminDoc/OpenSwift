package com.openswift.keyboard.engine

import android.content.Context
import com.openswift.keyboard.data.KeyboardLanguage
import com.openswift.keyboard.data.KeyboardLanguages

/**
 * Multilingual dictionary loader for bundled offline dictionaries.
 * Predictors are loaded lazily so unused language packs do not add first-open work.
 */
class MultilingualPredictor(private val ctx: Context) {

    private data class Entry(
        val wordList: WordList,
        val userDictionary: UserDictionary,
        val predictor: Predictor
    )

    private val entries: MutableMap<String, Entry> = mutableMapOf()

    fun suggest(lang: String, prefix: String, previousWord: String?, limit: Int = 3): List<String> {
        return entryFor(lang).predictor.suggest(prefix, previousWord, limit)
    }

    fun autoCorrect(lang: String, typed: String, previousWord: String?): String {
        return entryFor(lang).predictor.autoCorrect(typed, previousWord)
    }

    fun wordList(lang: String): WordList = entryFor(lang).wordList

    fun userDictionary(lang: String): UserDictionary = entryFor(lang).userDictionary

    fun supportedLanguages(): List<KeyboardLanguage> = KeyboardLanguages.all

    private fun entryFor(lang: String): Entry {
        val language = KeyboardLanguages.byCode(lang)
        return entries.getOrPut(language.code) {
            val wordList = WordList(ctx, language.wordListRes)
            val userDict = UserDictionary(ctx, language.code)
            Entry(wordList, userDict, Predictor(wordList, userDict))
        }
    }
}

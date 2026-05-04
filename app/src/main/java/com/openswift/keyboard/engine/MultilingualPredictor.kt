package com.openswift.keyboard.engine

import android.content.Context

/**
 * Multilingual dictionary loader: supports multiple languages + English.
 * Currently ships English; additional languages can be loaded from raw resources
 * or external data sources.
 */
class MultilingualPredictor(ctx: Context) {

    private val dictionaries: MutableMap<String, WordList> = mutableMapOf()
    private val predictors: MutableMap<String, Predictor> = mutableMapOf()

    init {
        // Load English by default
        val enWordList = WordList(ctx)
        val enUserDict = UserDictionary(ctx)
        dictionaries["en"] = enWordList
        predictors["en"] = Predictor(enWordList, enUserDict)
    }

    fun suggest(lang: String, prefix: String, previousWord: String?, limit: Int = 3): List<String> {
        val predictor = predictors[lang] ?: predictors["en"] ?: return emptyList()
        return predictor.suggest(prefix, previousWord, limit)
    }

    fun autoCorrect(lang: String, typed: String, previousWord: String?): String {
        val predictor = predictors[lang] ?: predictors["en"] ?: return typed
        return predictor.autoCorrect(typed, previousWord)
    }

    fun getSupportedLanguages(): List<String> = listOf("en") + (dictionaries.keys - "en")

    fun registerDictionary(langCode: String, wordList: WordList, userDict: UserDictionary) {
        dictionaries[langCode] = wordList
        predictors[langCode] = Predictor(wordList, userDict)
    }
}

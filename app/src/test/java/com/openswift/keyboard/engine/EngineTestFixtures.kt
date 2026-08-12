package com.openswift.keyboard.engine

internal class MemoryUserDictionaryStorage(
    var value: String? = null
) : UserDictionaryStorage {
    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }
}

internal fun emptyUserDictionary(): UserDictionary =
    UserDictionary(MemoryUserDictionaryStorage())

package com.openswift.keyboard.layout

import android.content.Context
import com.openswift.keyboard.data.CustomizationPackageParser
import com.openswift.keyboard.data.ImportedLayout
import com.openswift.keyboard.data.SecurePreferences
import com.openswift.keyboard.data.TypedDataStores

class CustomLayoutStore(context: Context) {
    private val preferences = SecurePreferences.open(context, TypedDataStores.CUSTOM_LAYOUTS)

    fun save(layout: ImportedLayout) {
        preferences.edit()
            .putString(layout.id, CustomizationPackageParser.encodeLayout(layout))
            .apply()
    }

    fun load(id: String): ImportedLayout? {
        val raw = preferences.getString(id, null) ?: return null
        return runCatching { CustomizationPackageParser.decodeLayout(raw) }
            .getOrNull()
            ?.takeIf { it.id == id }
    }

    fun list(): List<ImportedLayout> = preferences.all.keys
        .asSequence()
        .filter { it.startsWith(CUSTOM_LAYOUT_PREFIX) }
        .sorted()
        .mapNotNull(::load)
        .toList()

    fun delete(id: String) {
        preferences.edit().remove(id).apply()
    }

    companion object {
        const val CUSTOM_LAYOUT_PREFIX = "custom_layout_"
    }
}

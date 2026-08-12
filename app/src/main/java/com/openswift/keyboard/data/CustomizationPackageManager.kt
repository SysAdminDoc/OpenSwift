package com.openswift.keyboard.data

import android.content.Context
import com.openswift.keyboard.layout.CustomLayoutStore
import com.openswift.keyboard.theme.ThemeEditor

class CustomizationPackageManager(context: Context) {
    private val layouts = CustomLayoutStore(context)
    private val themes = ThemeEditor(context)

    data class ImportSummary(
        val packageName: String,
        val themeCount: Int,
        val layoutCount: Int,
    ) {
        fun asMessage(): String =
            "Imported $packageName: $themeCount theme(s), $layoutCount layout(s)"
    }

    fun importJson(raw: String): ImportSummary {
        val parsed = CustomizationPackageParser.parse(raw)
        parsed.themes.forEach(themes::saveImported)
        parsed.layouts.forEach(layouts::save)
        return ImportSummary(
            packageName = parsed.name,
            themeCount = parsed.themes.size,
            layoutCount = parsed.layouts.size,
        )
    }
}

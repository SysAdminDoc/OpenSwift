package com.openswift.keyboard.data

import com.openswift.keyboard.layout.Key
import com.openswift.keyboard.layout.KeyCode
import com.openswift.keyboard.layout.KeyLayout
import com.openswift.keyboard.theme.KbTheme
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CustomizationPackageException(message: String) : IllegalArgumentException(message)

data class ImportedLayout(
    val id: String,
    val name: String,
    val layout: KeyLayout,
)

data class ParsedCustomizationPackage(
    val name: String,
    val themes: List<KbTheme>,
    val layouts: List<ImportedLayout>,
)

object CustomizationPackageParser {
    const val FORMAT = "openswift.customization"
    const val SCHEMA_VERSION = 1
    const val MAX_PACKAGE_BYTES = 512 * 1024
    private const val MAX_ITEMS_PER_TYPE = 10
    private const val MAX_KEYS = 64
    private val THEME_ID = Regex("^custom_[a-z][a-z0-9_]{0,39}$")
    private val LAYOUT_ID = Regex("^custom_layout_[a-z][a-z0-9_]{0,32}$")
    private val HEX_COLOR = Regex("^[0-9A-Fa-f]+$")

    fun parse(raw: String): ParsedCustomizationPackage {
        validate(raw.isNotBlank()) { "The package is empty." }
        validate(raw.toByteArray(Charsets.UTF_8).size <= MAX_PACKAGE_BYTES) {
            "The package is larger than 512 KiB."
        }
        val root = try {
            JSONObject(raw)
        } catch (_: JSONException) {
            throw CustomizationPackageException("This file is not valid JSON.")
        }
        validate(requiredString(root, "format", "format") == FORMAT) {
            "Unsupported package format; expected \"$FORMAT\"."
        }
        validate(root.optInt("schemaVersion", -1) == SCHEMA_VERSION) {
            "Unsupported package schema version; this build supports version $SCHEMA_VERSION."
        }
        val name = requiredString(root, "name", "name", maxLength = 80)
        val themeArray = optionalArray(root, "themes")
        val layoutArray = optionalArray(root, "layouts")
        validate(themeArray.length() > 0 || layoutArray.length() > 0) {
            "The package must contain at least one theme or layout."
        }
        validate(themeArray.length() <= MAX_ITEMS_PER_TYPE) {
            "A package can contain at most $MAX_ITEMS_PER_TYPE themes."
        }
        validate(layoutArray.length() <= MAX_ITEMS_PER_TYPE) {
            "A package can contain at most $MAX_ITEMS_PER_TYPE layouts."
        }

        val themes = buildList {
            val ids = mutableSetOf<String>()
            for (index in 0 until themeArray.length()) {
                val theme = parseTheme(requiredObject(themeArray, index, "themes[$index]"), index)
                validate(ids.add(theme.id)) { "Theme id \"${theme.id}\" appears more than once." }
                add(theme)
            }
        }
        val layouts = buildList {
            val ids = mutableSetOf<String>()
            for (index in 0 until layoutArray.length()) {
                val layout = parseLayout(
                    requiredObject(layoutArray, index, "layouts[$index]"),
                    "layouts[$index]",
                )
                validate(ids.add(layout.id)) { "Layout id \"${layout.id}\" appears more than once." }
                add(layout)
            }
        }
        return ParsedCustomizationPackage(name, themes, layouts)
    }

    fun encodeLayout(layout: ImportedLayout): String = JSONObject().apply {
        put("id", layout.id)
        put("name", layout.name)
        put("rows", JSONArray().also { rows ->
            layout.layout.rows.forEach { row ->
                rows.put(JSONArray().also { keys -> row.forEach { keys.put(encodeKey(it)) } })
            }
        })
    }.toString()

    fun decodeLayout(raw: String): ImportedLayout = try {
        parseLayout(JSONObject(raw), "stored layout")
    } catch (error: JSONException) {
        throw CustomizationPackageException("Stored custom layout is not valid JSON: ${error.message}")
    }

    private fun parseTheme(obj: JSONObject, index: Int): KbTheme {
        val path = "themes[$index]"
        val id = requiredString(obj, "id", "$path.id", maxLength = 47)
        validate(THEME_ID.matches(id)) {
            "$path.id must start with custom_ and contain only lowercase letters, numbers, or underscores."
        }
        val name = requiredString(obj, "name", "$path.name", maxLength = 48)
        val colors = requiredObject(obj, "colors", "$path.colors")
        return KbTheme(
            id = id,
            name = name,
            background = requiredColor(colors, "background", "$path.colors.background"),
            keyBackground = requiredColor(colors, "keyBackground", "$path.colors.keyBackground"),
            keyModifierBackground = requiredColor(
                colors,
                "keyModifierBackground",
                "$path.colors.keyModifierBackground",
            ),
            keyText = requiredColor(colors, "keyText", "$path.colors.keyText"),
            keyAccent = requiredColor(colors, "keyAccent", "$path.colors.keyAccent"),
            suggestionBg = requiredColor(
                colors,
                "suggestionBackground",
                "$path.colors.suggestionBackground",
            ),
            suggestionText = requiredColor(colors, "suggestionText", "$path.colors.suggestionText"),
            gestureTrail = requiredColor(colors, "gestureTrail", "$path.colors.gestureTrail"),
        )
    }

    private fun parseLayout(obj: JSONObject, path: String): ImportedLayout {
        val id = requiredString(obj, "id", "$path.id", maxLength = 47)
        validate(LAYOUT_ID.matches(id)) {
            "$path.id must start with custom_layout_ and contain only lowercase letters, numbers, or underscores."
        }
        val name = requiredString(obj, "name", "$path.name", maxLength = 48)
        val rows = requiredArray(obj, "rows", "$path.rows")
        validate(rows.length() in 3..6) { "$path.rows must contain between 3 and 6 rows." }

        var totalKeys = 0
        val characterLabels = mutableSetOf<String>()
        val parsedRows = buildList {
            for (rowIndex in 0 until rows.length()) {
                val rowPath = "$path.rows[$rowIndex]"
                val row = requiredArray(rows, rowIndex, rowPath)
                validate(row.length() in 2..16) { "$rowPath must contain between 2 and 16 keys." }
                totalKeys += row.length()
                validate(totalKeys <= MAX_KEYS) { "$path contains more than $MAX_KEYS keys." }
                add(buildList {
                    for (keyIndex in 0 until row.length()) {
                        val keyPath = "$rowPath[$keyIndex]"
                        val key = parseKey(requiredObject(row, keyIndex, keyPath), keyPath)
                        if (key.code >= 0) {
                            validate(characterLabels.add(key.label)) {
                                "$keyPath duplicates character key \"${key.label}\"."
                            }
                        }
                        add(key)
                    }
                })
            }
        }
        val codes = parsedRows.flatten().map(Key::code)
        REQUIRED_ACTIONS.forEach { (code, action) ->
            validate(codes.count { it == code } == 1) {
                "$path must contain exactly one key with action \"$action\"."
            }
        }
        validate(codes.count { it >= 0 } >= 10) { "$path must contain at least 10 character keys." }
        return ImportedLayout(id, name, KeyLayout(id, parsedRows))
    }

    private fun parseKey(obj: JSONObject, path: String): Key {
        val action = obj.optString("action", "character")
        val label = stringAllowingEmpty(obj, "label", "$path.label", maxLength = 12)
        val width = when (val value = obj.opt("width")) {
            null, JSONObject.NULL -> 1f
            is Number -> value.toFloat()
            else -> throw CustomizationPackageException("$path.width must be a number.")
        }
        validate(width.isFinite() && width in 0.25f..5f) {
            "$path.width must be between 0.25 and 5."
        }
        val code = when (action) {
            "character" -> {
                validate(label.length == 1 && !label[0].isSurrogate()) {
                    "$path.label must be one basic Unicode character when action is \"character\"."
                }
                validate(!label[0].isWhitespace() && !label[0].isISOControl()) {
                    "$path.label cannot be whitespace or a control character."
                }
                label[0].code
            }
            "spacer" -> {
                validate(label.isEmpty()) { "$path.label must be empty when action is \"spacer\"." }
                SPACER_CODE
            }
            else -> SPECIAL_ACTIONS[action]
                ?: throw CustomizationPackageException(
                    "$path.action \"$action\" is not supported. Supported actions: " +
                        (listOf("character", "spacer") + SPECIAL_ACTIONS.keys).joinToString(),
                )
        }
        if (action != "spacer") {
            validate(label.isNotBlank()) { "$path.label cannot be blank." }
        }
        val popups = optionalArray(obj, "popup").let { array ->
            validate(array.length() <= 12) { "$path.popup can contain at most 12 items." }
            buildList {
                for (index in 0 until array.length()) {
                    val popupPath = "$path.popup[$index]"
                    val popup = array.opt(index) as? String
                        ?: throw CustomizationPackageException("$popupPath must be a string.")
                    validate(popup.length == 1 && !popup[0].isSurrogate()) {
                        "$popupPath must be one basic Unicode character."
                    }
                    add(popup)
                }
            }
        }
        validate(action == "character" || popups.isEmpty()) {
            "$path.popup is only supported for character keys."
        }
        return Key(
            label = label,
            code = code,
            widthWeight = width,
            popup = popups,
            isModifier = action in MODIFIER_ACTIONS,
        )
    }

    private fun encodeKey(key: Key): JSONObject = JSONObject().apply {
        put("label", key.label)
        actionForCode(key.code)?.let { put("action", it) }
        if (key.widthWeight != 1f) put("width", key.widthWeight.toDouble())
        if (key.popup.isNotEmpty()) put("popup", JSONArray(key.popup))
    }

    private fun actionForCode(code: Int): String? = when {
        code == SPACER_CODE -> "spacer"
        code >= 0 -> null
        else -> SPECIAL_ACTIONS.entries.firstOrNull { it.value == code }?.key
            ?: throw CustomizationPackageException("Cannot encode unsupported key code $code.")
    }

    private fun requiredColor(obj: JSONObject, key: String, path: String): Int {
        val value = requiredString(obj, key, path, maxLength = 9)
        validate(value.startsWith('#') && value.length in setOf(7, 9)) {
            "$path must be #RRGGBB or #AARRGGBB."
        }
        val hex = value.drop(1)
        validate(HEX_COLOR.matches(hex)) { "$path contains non-hexadecimal characters." }
        val argb = if (hex.length == 6) "FF$hex" else hex
        return argb.toLong(16).toInt()
    }

    private fun optionalArray(obj: JSONObject, key: String): JSONArray {
        if (!obj.has(key)) return JSONArray()
        return obj.optJSONArray(key)
            ?: throw CustomizationPackageException("$key must be an array.")
    }

    private fun requiredArray(obj: JSONObject, key: String, path: String): JSONArray =
        obj.optJSONArray(key) ?: throw CustomizationPackageException("$path must be an array.")

    private fun requiredArray(array: JSONArray, index: Int, path: String): JSONArray =
        array.optJSONArray(index) ?: throw CustomizationPackageException("$path must be an array.")

    private fun requiredObject(obj: JSONObject, key: String, path: String): JSONObject =
        obj.optJSONObject(key) ?: throw CustomizationPackageException("$path must be an object.")

    private fun requiredObject(array: JSONArray, index: Int, path: String): JSONObject =
        array.optJSONObject(index) ?: throw CustomizationPackageException("$path must be an object.")

    private fun requiredString(
        obj: JSONObject,
        key: String,
        path: String,
        maxLength: Int = 120,
    ): String {
        val value = stringAllowingEmpty(obj, key, path, maxLength)
        validate(value.isNotBlank()) { "$path cannot be blank." }
        return value
    }

    private fun stringAllowingEmpty(
        obj: JSONObject,
        key: String,
        path: String,
        maxLength: Int,
    ): String {
        val value = obj.opt(key) as? String
            ?: throw CustomizationPackageException("$path must be a string.")
        validate(value.length <= maxLength) { "$path cannot exceed $maxLength characters." }
        return value
    }

    private inline fun validate(condition: Boolean, message: () -> String) {
        if (!condition) throw CustomizationPackageException(message())
    }

    private const val SPACER_CODE = -100
    private val SPECIAL_ACTIONS = linkedMapOf(
        "shift" to KeyCode.SHIFT,
        "delete" to KeyCode.DELETE,
        "enter" to KeyCode.ENTER,
        "space" to KeyCode.SPACE,
        "symbols" to KeyCode.SYMBOLS,
        "clipboard" to KeyCode.CLIPBOARD,
        "comma" to KeyCode.COMMA,
        "period" to KeyCode.PERIOD,
        "emoji" to KeyCode.EMOJI,
        "settings" to KeyCode.SETTINGS,
    )
    private val REQUIRED_ACTIONS = linkedMapOf(
        KeyCode.SHIFT to "shift",
        KeyCode.DELETE to "delete",
        KeyCode.ENTER to "enter",
        KeyCode.SPACE to "space",
        KeyCode.SYMBOLS to "symbols",
    )
    private val MODIFIER_ACTIONS = setOf(
        "shift",
        "delete",
        "enter",
        "symbols",
        "clipboard",
        "emoji",
        "settings",
    )
}

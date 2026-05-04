package com.openswift.keyboard.theme

import androidx.compose.ui.graphics.Color

data class KbTheme(
    val id: String,
    val name: String,
    val background: Int,
    val keyBackground: Int,
    val keyModifierBackground: Int,
    val keyText: Int,
    val keyAccent: Int,
    val suggestionBg: Int,
    val suggestionText: Int,
    val gestureTrail: Int
) {
    companion object {
        fun rgb(r: Int, g: Int, b: Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        fun rgba(r: Int, g: Int, b: Int, a: Int) = (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

object Themes {

    val Amoled = KbTheme(
        id = "amoled",
        name = "AMOLED Black",
        background = KbTheme.rgb(0, 0, 0),
        keyBackground = KbTheme.rgb(18, 18, 20),
        keyModifierBackground = KbTheme.rgb(8, 8, 10),
        keyText = KbTheme.rgb(240, 240, 245),
        keyAccent = KbTheme.rgb(122, 162, 247),
        suggestionBg = KbTheme.rgb(0, 0, 0),
        suggestionText = KbTheme.rgb(220, 220, 230),
        gestureTrail = KbTheme.rgb(187, 154, 247)
    )

    val Mocha = KbTheme(
        id = "mocha",
        name = "Catppuccin Mocha",
        background = KbTheme.rgb(30, 30, 46),
        keyBackground = KbTheme.rgb(49, 50, 68),
        keyModifierBackground = KbTheme.rgb(24, 24, 37),
        keyText = KbTheme.rgb(205, 214, 244),
        keyAccent = KbTheme.rgb(137, 180, 250),
        suggestionBg = KbTheme.rgb(24, 24, 37),
        suggestionText = KbTheme.rgb(205, 214, 244),
        gestureTrail = KbTheme.rgb(203, 166, 247)
    )

    val GithubDark = KbTheme(
        id = "github_dark",
        name = "GitHub Dark",
        background = KbTheme.rgb(13, 17, 23),
        keyBackground = KbTheme.rgb(33, 38, 45),
        keyModifierBackground = KbTheme.rgb(22, 27, 34),
        keyText = KbTheme.rgb(201, 209, 217),
        keyAccent = KbTheme.rgb(88, 166, 255),
        suggestionBg = KbTheme.rgb(13, 17, 23),
        suggestionText = KbTheme.rgb(201, 209, 217),
        gestureTrail = KbTheme.rgb(125, 191, 255)
    )

    val SwiftDark = KbTheme(
        id = "swift_dark",
        name = "Swift Dark",
        background = KbTheme.rgb(20, 26, 38),
        keyBackground = KbTheme.rgb(36, 46, 64),
        keyModifierBackground = KbTheme.rgb(20, 26, 38),
        keyText = KbTheme.rgb(255, 255, 255),
        keyAccent = KbTheme.rgb(0, 174, 239),
        suggestionBg = KbTheme.rgb(15, 20, 30),
        suggestionText = KbTheme.rgb(220, 230, 240),
        gestureTrail = KbTheme.rgb(0, 174, 239)
    )

    val MaterialLight = KbTheme(
        id = "material_light",
        name = "Material Light",
        background = KbTheme.rgb(220, 226, 234),
        keyBackground = KbTheme.rgb(255, 255, 255),
        keyModifierBackground = KbTheme.rgb(192, 200, 212),
        keyText = KbTheme.rgb(20, 20, 20),
        keyAccent = KbTheme.rgb(98, 0, 238),
        suggestionBg = KbTheme.rgb(220, 226, 234),
        suggestionText = KbTheme.rgb(20, 20, 20),
        gestureTrail = KbTheme.rgb(98, 0, 238)
    )

    val Pixel = KbTheme(
        id = "pixel",
        name = "Pixel",
        background = KbTheme.rgb(15, 16, 17),
        keyBackground = KbTheme.rgb(15, 16, 17),
        keyModifierBackground = KbTheme.rgb(15, 16, 17),
        keyText = KbTheme.rgb(232, 234, 237),
        keyAccent = KbTheme.rgb(138, 180, 248),
        suggestionBg = KbTheme.rgb(15, 16, 17),
        suggestionText = KbTheme.rgb(232, 234, 237),
        gestureTrail = KbTheme.rgb(138, 180, 248)
    )

    val Nord = KbTheme(
        id = "nord",
        name = "Nord",
        background = KbTheme.rgb(46, 52, 64),
        keyBackground = KbTheme.rgb(59, 66, 82),
        keyModifierBackground = KbTheme.rgb(36, 41, 51),
        keyText = KbTheme.rgb(236, 239, 244),
        keyAccent = KbTheme.rgb(136, 192, 208),
        suggestionBg = KbTheme.rgb(36, 41, 51),
        suggestionText = KbTheme.rgb(216, 222, 233),
        gestureTrail = KbTheme.rgb(143, 188, 187)
    )

    val Dracula = KbTheme(
        id = "dracula",
        name = "Dracula",
        background = KbTheme.rgb(40, 42, 54),
        keyBackground = KbTheme.rgb(68, 71, 90),
        keyModifierBackground = KbTheme.rgb(40, 42, 54),
        keyText = KbTheme.rgb(248, 248, 242),
        keyAccent = KbTheme.rgb(189, 147, 249),
        suggestionBg = KbTheme.rgb(40, 42, 54),
        suggestionText = KbTheme.rgb(248, 248, 242),
        gestureTrail = KbTheme.rgb(139, 233, 253)
    )

    val Tokyonight = KbTheme(
        id = "tokyonight",
        name = "Tokyo Night",
        background = KbTheme.rgb(26, 27, 38),
        keyBackground = KbTheme.rgb(41, 44, 60),
        keyModifierBackground = KbTheme.rgb(26, 27, 38),
        keyText = KbTheme.rgb(192, 202, 245),
        keyAccent = KbTheme.rgb(122, 162, 247),
        suggestionBg = KbTheme.rgb(26, 27, 38),
        suggestionText = KbTheme.rgb(173, 187, 230),
        gestureTrail = KbTheme.rgb(158, 206, 106)
    )

    val all: List<KbTheme> = listOf(Amoled, Mocha, GithubDark, SwiftDark, MaterialLight, Pixel, Nord, Dracula, Tokyonight)

    fun byId(id: String): KbTheme = all.firstOrNull { it.id == id } ?: Amoled
}

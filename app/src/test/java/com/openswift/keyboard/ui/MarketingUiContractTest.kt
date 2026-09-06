package com.openswift.keyboard.ui

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketingUiContractTest {

    @Test
    fun featureCardsUseExplicitThemeTextColor() {
        val featureCard = source("java/com/openswift/keyboard/ui/FeatureCard.kt")

        assertTrue(featureCard.contains("textColor: Color"))
        assertTrue(featureCard.contains("color = textColor"))
        assertFalse(featureCard.contains("LocalContentColor.current"))
    }

    @Test
    fun managementScreensUseBrandAndMaterialIconsInsteadOfEmoji() {
        val files = listOf("HomeUI.kt", "MainActivity.kt", "PrivacyUI.kt", "AboutUI.kt")
        val source = files.joinToString("\n") { source("java/com/openswift/keyboard/ui/$it") }
        val retiredEmoji = listOf("⌨️", "🏠", "⚙️", "🔒", "ℹ️", "🎨", "👆", "⚡", "📋", "📚", "⚠️", "❤️")

        retiredEmoji.forEach { emoji -> assertFalse("Found retired UI emoji: $emoji", source.contains(emoji)) }
        assertTrue(source.contains("R.drawable.openswift_brand"))
        assertTrue(source.contains("Icons.Filled.Home"))
        assertTrue(source.contains("Icons.Filled.SettingsNavigationIcon"))
        assertTrue(source.contains("Icons.Filled.Lock"))
        assertTrue(source.contains("Icons.Filled.Info"))
        assertTrue(source.contains("Icons.Filled.Check"))
        assertFalse(source.contains("Text(\"✓\""))
    }

    @Test
    fun launcherResourcesUseTheNewBrandSystem() {
        val manifest = source("AndroidManifest.xml")
        val background = source("res/values/ic_launcher_background.xml")
        val brand = File("src/main/res/drawable-nodpi/openswift_brand.png")

        assertTrue(manifest.contains("android:roundIcon=\"@mipmap/ic_launcher_round\""))
        assertTrue(background.contains("#07152F"))
        assertTrue(brand.isFile)
        assertTrue(brand.length() > 10_000L)
    }

    @Test
    fun keyboardUsesCompactSuggestionsAndRoundedKeys() {
        val keyboardView = source("java/com/openswift/keyboard/view/KeyboardView.kt")

        assertTrue(keyboardView.contains("keyHeightPx * 0.82f"))
        assertTrue(keyboardView.contains("val pillWidth ="))
        assertTrue(keyboardView.contains("val keyCornerRadius = 6f * density"))
        assertTrue(keyboardView.contains("R.drawable.ic_content_paste"))
        assertTrue(keyboardView.contains("R.drawable.ic_keyboard_capslock"))
        assertTrue(keyboardView.contains("R.drawable.ic_backspace"))
        assertTrue(keyboardView.contains("R.drawable.ic_keyboard_return"))
        assertTrue(keyboardView.contains("R.drawable.ic_sentiment_satisfied"))
        assertTrue(keyboardView.contains("R.drawable.ic_settings"))
        assertTrue(keyboardView.contains("key.code == KC.SPACER"))
        assertFalse(keyboardView.contains("keyHeightPx * 1.2f"))
        val layouts = source("java/com/openswift/keyboard/layout/Layouts.kt")
        listOf("📋", "⇧", "⌫", "⏎").forEach { glyph ->
            assertFalse("Found retired keyboard glyph: $glyph", layouts.contains(glyph))
        }
        assertTrue(layouts.contains("Key(\"Emoji\", KC.EMOJI"))
        assertTrue(layouts.contains("Key(\"Settings\", KC.SETTINGS"))
    }

    @Test
    fun canvasPanelsUseMaterialActionIconsInsteadOfTextSymbols() {
        val clipboard = source("java/com/openswift/keyboard/ui/ClipboardView.kt")
        val emoji = source("java/com/openswift/keyboard/ui/EmojiView.kt")

        assertTrue(clipboard.contains("R.drawable.ic_close"))
        assertTrue(emoji.contains("R.drawable.ic_close"))
        assertTrue(emoji.contains("R.drawable.ic_backspace"))
        assertTrue(emoji.contains("R.drawable.ic_star"))
        assertTrue(emoji.contains("var onClose: (() -> Unit)?"))
        assertTrue(source("java/com/openswift/keyboard/OpenSwiftIME.kt").contains("emojiView.onClose"))
        assertFalse(clipboard.contains("drawText(\"×\""))
        assertFalse(emoji.contains("drawText(\"Del\""))
        assertFalse(emoji.contains("drawText(\"*\""))
        assertTrue(File("src/main/res/drawable/ic_close.xml").isFile)
        assertTrue(File("src/main/res/drawable/ic_star.xml").isFile)
    }

    private fun source(relativePath: String): String {
        val file = File("src/main/$relativePath")
        check(file.isFile) { "Missing source file: ${file.absolutePath}" }
        return file.readText()
    }
}

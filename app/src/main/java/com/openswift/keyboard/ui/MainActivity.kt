package com.openswift.keyboard.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.data.KeyboardLanguages
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.theme.Themes

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = Settings(this@MainActivity)
            MainUI(settings, this@MainActivity)
        }
    }
}

@Composable
fun MainUI(settings: Settings, context: android.content.Context) {
    var activeTab by remember { mutableStateOf(0) }
    
    val theme = Themes.byId(settings.theme)
    val bgColor = Color(theme.background)
    val keyBgColor = Color(theme.keyBackground)
    val textColor = Color(theme.keyText)
    val accentColor = Color(theme.keyAccent)
    val surfaceColor = SemanticColors.getSurfaceColor(theme.background.toLong(), theme.keyBackground.toLong())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Tab content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> HomeUI(theme, bgColor, keyBgColor, textColor, accentColor)
                1 -> EnhancedSettingsUI(settings, bgColor, textColor, accentColor)
                2 -> PrivacyUI(
                    ClipboardHistory(context),
                    UserDictionary(context, settings.language),
                    bgColor,
                    textColor,
                    accentColor
                )
                3 -> AboutUI(bgColor, textColor, accentColor)
            }
        }
        
        // Premium bottom navigation bar
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = surfaceColor.copy(alpha = 0.95f),
            contentColor = textColor,
            tonalElevation = Elevations.md
        ) {
            val items = listOf(
                Pair("🏠", "Home"),
                Pair("⚙️", "Settings"),
                Pair("🔒", "Privacy"),
                Pair("ℹ️", "About")
            )
            
            items.forEachIndexed { index, (icon, label) ->
                NavigationBarItem(
                    icon = {
                        Text(
                            icon,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = if (activeTab == index) 2.dp else 0.dp)
                        )
                    },
                    label = {
                        Text(
                            label,
                            style = AppTypography.labelSmall,
                            fontSize = if (activeTab == index) 12.sp else 11.sp
                        )
                    },
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = accentColor.copy(alpha = 0.12f),
                        selectedIconColor = accentColor,
                        selectedTextColor = accentColor,
                        unselectedIconColor = textColor.copy(alpha = 0.6f),
                        unselectedTextColor = textColor.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EnhancedSettingsUI(settings: Settings, bgColor: Color, textColor: Color, accentColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = textColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Appearance Section
        SettingsSection(
            title = "🎨 Appearance",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ColorCustomizer(
                currentThemeId = settings.theme,
                onThemeChange = { settings.theme = it },
                bgColor = bgColor,
                textColor = textColor,
                accentColor = accentColor,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keyboard Settings
        SettingsSection(
            title = "⌨️ Keyboard",
            textColor = textColor,
            accentColor = accentColor
        ) {
            SettingsList(
                listOf(
                    "Language" to KeyboardLanguages.all.map { it.code to it.name },
                    "Layout" to listOf("qwerty" to "QWERTY", "qwertz" to "QWERTZ", "azerty" to "AZERTY"),
                    "Height" to emptyList()
                ),
                settings,
                textColor,
                accentColor
            )
        }

        // Typing & Accuracy
        SettingsSection(
            title = "✨ Typing & Accuracy",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Glide Typing", settings.glideEnabled) { settings.glideEnabled = it }
            ToggleOption("Auto-Correct", settings.autoCorrect) { settings.autoCorrect = it }
            ToggleOption("Auto-Capitalize", settings.autoCapitalize) { settings.autoCapitalize = it }
        }

        // Feedback
        SettingsSection(
            title = "🔊 Feedback",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Haptic Feedback", settings.hapticFeedback) { settings.hapticFeedback = it }
            ToggleOption("Sound Effects", settings.soundFeedback) { settings.soundFeedback = it }
        }

        // Accessibility
        SettingsSection(
            title = "♿ Accessibility",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Reduce Motion", settings.reducedMotion) { settings.reducedMotion = it }
        }

        // Advanced
        SettingsSection(
            title = "🔧 Advanced",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Power Saving Mode", settings.powerSaveMode) { settings.powerSaveMode = it }
            ToggleOption("Clipboard History", settings.clipboardEnabled) { settings.clipboardEnabled = it }
            ToggleOption("Per-App Tint", settings.perAppTint) { settings.perAppTint = it }
            ToggleOption("Incognito Mode", settings.incognitoMode) { settings.incognitoMode = it }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    textColor: Color,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            title,
            style = AppTypography.headlineSmall,
            color = accentColor,
            modifier = Modifier.padding(start = Spacing.sm)
        )
        Column(
            modifier = Modifier.padding(start = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsList(
    items: List<Pair<String, List<Pair<String, String>>>>,
    settings: Settings,
    textColor: Color,
    accentColor: Color
) {
    var selectedLanguage by remember { mutableStateOf(settings.language) }
    var selectedLayout by remember { mutableStateOf(settings.layout) }

    items.forEach { (label, options) ->
        if (options.isNotEmpty()) {
            Text(label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            options.forEach { (id, name) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, color = textColor)
                    RadioButton(
                        selected = when (label) {
                            "Language" -> selectedLanguage == id
                            "Layout" -> selectedLayout == id
                            else -> false
                        },
                        onClick = {
                            when (label) {
                                "Language" -> {
                                    settings.language = id
                                    selectedLanguage = settings.language
                                    selectedLayout = settings.layout
                                }
                                "Layout" -> {
                                    settings.layout = id
                                    selectedLayout = id
                                }
                            }
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleOption(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = AppTypography.bodyMedium,
            color = LocalContentColor.current,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            modifier = Modifier.scale(0.95f)
        )
    }
}

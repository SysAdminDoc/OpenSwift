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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.theme.Themes

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = Settings(this@MainActivity)
            MainUI(settings)
        }
    }
}

@Composable
fun MainUI(settings: Settings) {
    var activeTab by remember { mutableStateOf(0) }
    
    val theme = Themes.byId(settings.theme)
    val bgColor = Color(theme.background)
    val keyBgColor = Color(theme.keyBackground)
    val textColor = Color(theme.keyText)
    val accentColor = Color(theme.keyAccent)

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
                2 -> AboutUI(bgColor, textColor, accentColor)
            }
        }
        
        // Bottom navigation
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = keyBgColor.copy(alpha = 0.8f),
            contentColor = textColor
        ) {
            NavigationBarItem(
                icon = { Text("🏠", fontSize = 20.sp) },
                label = { Text("Home") },
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = accentColor.copy(alpha = 0.2f),
                    selectedIconColor = accentColor,
                    selectedTextColor = accentColor
                )
            )
            NavigationBarItem(
                icon = { Text("⚙️", fontSize = 20.sp) },
                label = { Text("Settings") },
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = accentColor.copy(alpha = 0.2f),
                    selectedIconColor = accentColor,
                    selectedTextColor = accentColor
                )
            )
            NavigationBarItem(
                icon = { Text("ℹ️", fontSize = 20.sp) },
                label = { Text("About") },
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = accentColor.copy(alpha = 0.2f),
                    selectedIconColor = accentColor,
                    selectedTextColor = accentColor
                )
            )
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        content()
    }
}

@Composable
fun SettingsList(
    items: List<Pair<String, List<Pair<String, String>>>>,
    settings: Settings,
    textColor: Color,
    accentColor: Color
) {
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
                        selected = if (label == "Layout") settings.layout == id else false,
                        onClick = { if (label == "Layout") settings.layout = id },
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = LocalContentColor.current)
        Switch(
            checked = value,
            onCheckedChange = onToggle
        )
    }
}

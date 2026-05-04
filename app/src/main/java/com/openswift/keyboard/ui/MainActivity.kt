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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.theme.Themes
import com.openswift.keyboard.layout.Layouts

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = Settings(this@MainActivity)
            SettingsUI(settings)
        }
    }
}

@Composable
fun SettingsUI(settings: Settings) {
    val theme = Themes.byId(settings.theme)
    val bgColor = Color(theme.background)
    val keyBgColor = Color(theme.keyBackground)
    val textColor = Color(theme.keyText)
    val accentColor = Color(theme.keyAccent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "OpenSwift Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = textColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Theme selector
        Text("Theme", style = MaterialTheme.typography.titleMedium, color = textColor)
        Themes.all.forEach { t ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(t.name, color = textColor)
                RadioButton(
                    selected = settings.theme == t.id,
                    onClick = { settings.theme = t.id },
                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                )
            }
        }
        Divider(color = accentColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        // Layout selector
        Text("Keyboard Layout", style = MaterialTheme.typography.titleMedium, color = textColor)
        listOf("qwerty" to "QWERTY", "qwertz" to "QWERTZ", "azerty" to "AZERTY").forEach { (id, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = textColor)
                RadioButton(
                    selected = settings.layout == id,
                    onClick = { settings.layout = id },
                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                )
            }
        }
        Divider(color = accentColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        // Feature toggles
        var glide by remember { mutableStateOf(settings.glideEnabled) }
        var correct by remember { mutableStateOf(settings.autoCorrect) }
        var cap by remember { mutableStateOf(settings.autoCapitalize) }
        var haptic by remember { mutableStateOf(settings.hapticFeedback) }

        Text("Features", style = MaterialTheme.typography.titleMedium, color = textColor)
        ToggleOption("Glide Typing", glide) { glide = it; settings.glideEnabled = it }
        ToggleOption("Auto-Correct", correct) { correct = it; settings.autoCorrect = it }
        ToggleOption("Auto-Capitalize", cap) { cap = it; settings.autoCapitalize = it }
        ToggleOption("Haptic Feedback", haptic) { haptic = it; settings.hapticFeedback = it }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "OpenSwift v0.1.0",
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ToggleOption(label: String, value: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = LocalContentColor.current)
        Switch(
            checked = value,
            onCheckedChange = onToggle
        )
    }
}

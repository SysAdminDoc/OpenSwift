package com.openswift.keyboard.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.data.Settings
import com.openswift.keyboard.data.SnippetManager
import com.openswift.keyboard.theme.Themes
import com.openswift.keyboard.theme.ThemeEditor

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = Settings(this@SettingsActivity)
            val themeEditor = ThemeEditor(this@SettingsActivity)
            val snippets = SnippetManager(this@SettingsActivity)
            SettingsUI(settings, themeEditor, snippets)
        }
    }
}

@Composable
fun SettingsUI(
    settings: Settings,
    themeEditor: ThemeEditor,
    snippets: SnippetManager
) {
    val theme = Themes.byId(settings.theme)
    val bgColor = ComposeColor(theme.background)
    val keyBgColor = ComposeColor(theme.keyBackground)
    val textColor = ComposeColor(theme.keyText)
    val accentColor = ComposeColor(theme.keyAccent)

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Keyboard", "Themes", "Snippets")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = keyBgColor,
            contentColor = accentColor
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = { Text(label, color = textColor) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> KeyboardSettingsTab(settings, textColor, accentColor)
                1 -> ThemesTab(settings, themeEditor, textColor, accentColor)
                2 -> SnippetsTab(snippets, textColor, accentColor)
            }
        }
    }
}

@Composable
fun KeyboardSettingsTab(settings: Settings, textColor: ComposeColor, accentColor: ComposeColor) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Keyboard Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("Layout", style = MaterialTheme.typography.titleMedium, color = textColor)
        listOf("qwerty" to "QWERTY", "qwertz" to "QWERTZ", "azerty" to "AZERTY").forEach { (id, label) ->
            ToggleOption(label, settings.layout == id, textColor) { settings.layout = id }
        }
        Divider(color = accentColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        Text("Features", style = MaterialTheme.typography.titleMedium, color = textColor)
        var glide by remember { mutableStateOf(settings.glideEnabled) }
        var correct by remember { mutableStateOf(settings.autoCorrect) }
        var cap by remember { mutableStateOf(settings.autoCapitalize) }
        var haptic by remember { mutableStateOf(settings.hapticFeedback) }
        var sound by remember { mutableStateOf(settings.soundFeedback) }

        SwitchOption("Glide Typing", glide, textColor) { glide = it; settings.glideEnabled = it }
        SwitchOption("Auto-Correct", correct, textColor) { correct = it; settings.autoCorrect = it }
        SwitchOption("Auto-Capitalize", cap, textColor) { cap = it; settings.autoCapitalize = it }
        SwitchOption("Haptic Feedback", haptic, textColor) { haptic = it; settings.hapticFeedback = it }
        SwitchOption("Sound Feedback", sound, textColor) { sound = it; settings.soundFeedback = it }
    }
}

@Composable
fun ThemesTab(settings: Settings, themeEditor: ThemeEditor, textColor: ComposeColor, accentColor: ComposeColor) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Themes",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("Built-in Themes", style = MaterialTheme.typography.titleMedium, color = textColor)
        Themes.all.forEach { t ->
            ToggleOption(t.name, settings.theme == t.id, textColor) { settings.theme = t.id }
        }
        Divider(color = accentColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        Text("Custom Themes", style = MaterialTheme.typography.titleMedium, color = textColor)
        val custom = remember { themeEditor.listCustom() }
        custom.forEach { ct ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ct.name, color = textColor)
                IconButton(
                    onClick = { themeEditor.delete(ct.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun SnippetsTab(snippets: SnippetManager, textColor: ComposeColor, accentColor: ComposeColor) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Text Snippets",
            style = MaterialTheme.typography.headlineSmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text("Custom shortcuts: type trigger, auto-expand to full text", style = MaterialTheme.typography.bodySmall, color = textColor)
        Spacer(modifier = Modifier.height(12.dp))

        var triggerInput by remember { mutableStateOf("") }
        var textInput by remember { mutableStateOf("") }

        OutlinedTextField(
            value = triggerInput,
            onValueChange = { triggerInput = it },
            label = { Text("Trigger (e.g., 'omw')", color = textColor) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Expansion text", color = textColor) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Button(
            onClick = {
                if (triggerInput.isNotEmpty() && textInput.isNotEmpty()) {
                    snippets.add(triggerInput, textInput)
                    triggerInput = ""
                    textInput = ""
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
            Text("Add", modifier = Modifier.padding(start = 4.dp))
        }

        Divider(color = accentColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 16.dp))

        Text("Saved Snippets", style = MaterialTheme.typography.titleMedium, color = textColor)
        snippets.getAll().forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(s.trigger, style = MaterialTheme.typography.bodyMedium, color = accentColor)
                    Text(s.text.take(32), style = MaterialTheme.typography.bodySmall, color = textColor)
                }
                IconButton(
                    onClick = { snippets.remove(s.trigger) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = accentColor)
                }
            }
        }
    }
}

@Composable
fun ToggleOption(label: String, selected: Boolean, textColor: ComposeColor, onClick: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = textColor)
        RadioButton(
            selected = selected,
            onClick = { onClick(!selected) },
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun SwitchOption(label: String, value: Boolean, textColor: ComposeColor, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = textColor)
        Switch(checked = value, onCheckedChange = onToggle)
    }
}

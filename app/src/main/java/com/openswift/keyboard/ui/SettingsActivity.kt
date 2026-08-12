package com.openswift.keyboard.ui

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.data.KeyboardLanguages
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
            .safeDrawingPadding()
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SemanticColors.getSubtleAccent(accentColor, true),
            contentColor = accentColor,
            divider = { HorizontalDivider(color = accentColor.copy(alpha = Alphas.divider)) }
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            label,
                            color = textColor,
                            style = if (selectedTab == idx) AppTypography.labelLarge else AppTypography.labelMedium
                        )
                    },
                    modifier = Modifier.padding(vertical = Spacing.md)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg)
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
    var language by remember { mutableStateOf(settings.language) }
    var layout by remember { mutableStateOf(settings.layout) }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            "Keyboard Settings",
            style = AppTypography.headlineSmall,
            color = textColor
        )

        SettingsGroup(title = "Language") {
            KeyboardLanguages.all.forEach { option ->
                ToggleOption(option.name, language == option.code, textColor, accentColor) {
                    settings.language = option.code
                    language = settings.language
                    layout = settings.layout
                }
            }
        }

        SettingsGroup(title = "Layout") {
            listOf("qwerty" to "QWERTY", "qwertz" to "QWERTZ", "azerty" to "AZERTY").forEach { (id, label) ->
                ToggleOption(label, layout == id, textColor, accentColor) {
                    settings.layout = id
                    layout = id
                }
            }
        }

        SettingsGroup(title = "Features") {
            var glide by remember { mutableStateOf(settings.glideEnabled) }
            var correct by remember { mutableStateOf(settings.autoCorrect) }
            var languageDetection by remember { mutableStateOf(settings.languageDetection) }
            var cap by remember { mutableStateOf(settings.autoCapitalize) }
            var haptic by remember { mutableStateOf(settings.hapticFeedback) }
            var sound by remember { mutableStateOf(settings.soundFeedback) }
            var reducedMotion by remember { mutableStateOf(settings.reducedMotion) }

            SwitchOption("Glide Typing", glide, textColor, accentColor) { glide = it; settings.glideEnabled = it }
            SwitchOption("Auto-Correct", correct, textColor, accentColor) { correct = it; settings.autoCorrect = it }
            SwitchOption("Detect Language", languageDetection, textColor, accentColor) { languageDetection = it; settings.languageDetection = it }
            SwitchOption("Auto-Capitalize", cap, textColor, accentColor) { cap = it; settings.autoCapitalize = it }
            SwitchOption("Haptic Feedback", haptic, textColor, accentColor) { haptic = it; settings.hapticFeedback = it }
            SwitchOption("Sound Feedback", sound, textColor, accentColor) { sound = it; settings.soundFeedback = it }
            SwitchOption("Reduce Motion", reducedMotion, textColor, accentColor) { reducedMotion = it; settings.reducedMotion = it }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            title,
            style = AppTypography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alphas.secondary)
        )
        content()
    }
}

@Composable
fun ThemesTab(settings: Settings, themeEditor: ThemeEditor, textColor: ComposeColor, accentColor: ComposeColor) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            "Color Themes",
            style = AppTypography.headlineSmall,
            color = textColor
        )

        SettingsGroup(title = "Built-in Themes") {
            Themes.all.forEach { t ->
                ToggleOption(t.name, settings.theme == t.id, textColor, accentColor) { settings.theme = t.id }
            }
        }

        SettingsGroup(title = "Custom Themes") {
            val custom = remember { themeEditor.listCustom() }
            if (custom.isEmpty()) {
                Text(
                    "No custom themes yet",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = Alphas.secondary)
                )
            } else {
                custom.forEach { ct ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ct.name, color = textColor, style = AppTypography.bodyMedium)
                        IconButton(
                            onClick = { themeEditor.delete(ct.id) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = accentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnippetsTab(
    snippets: SnippetManager,
    textColor: ComposeColor,
    accentColor: ComposeColor,
    initialEditorOpen: Boolean = false
) {
    var allSnippets by remember { mutableStateOf(snippets.getAll()) }
    var editorOpen by remember { mutableStateOf(initialEditorOpen) }
    var editingSnippet by remember { mutableStateOf<SnippetManager.Snippet?>(null) }
    var trigger by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun openEditor(snippet: SnippetManager.Snippet? = null) {
        editingSnippet = snippet
        trigger = snippet?.trigger.orEmpty()
        replacement = snippet?.text.orEmpty()
        validationError = null
        editorOpen = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Text Snippets",
                style = AppTypography.headlineSmall,
                color = textColor
            )
            Button(
                onClick = { openEditor() },
                shape = Shapes.md
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(Spacing.sm))
                Text("Create", style = AppTypography.labelLarge)
            }
        }

        Text(
            "Type a trigger followed by space, enter, or punctuation to replace it.",
            style = AppTypography.bodySmall,
            color = textColor.copy(alpha = Alphas.secondary),
            modifier = Modifier.padding(bottom = Spacing.md)
        )

        if (allSnippets.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.md),
                shape = Shapes.md,
                colors = CardDefaults.cardColors(
                    containerColor = SemanticColors.getSubtleAccent(accentColor, true)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No snippets yet",
                        style = AppTypography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                    Button(
                        onClick = { openEditor() },
                        shape = Shapes.md
                    ) {
                        Text("Create Snippet", style = AppTypography.labelLarge)
                    }
                }
            }
        } else {
            allSnippets.forEach { snippet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shapes.md,
                    colors = CardDefaults.cardColors(
                        containerColor = SemanticColors.getSubtleAccent(accentColor, true)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(snippet.trigger, style = AppTypography.labelLarge, color = accentColor)
                            Text(
                                snippet.text.replace("\n", " ").take(80),
                                style = AppTypography.bodySmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = { openEditor(snippet) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit ${snippet.trigger}",
                                tint = accentColor
                            )
                        }
                        IconButton(
                            onClick = {
                                snippets.remove(snippet.trigger)
                                allSnippets = snippets.getAll()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete ${snippet.trigger}",
                                tint = accentColor
                            )
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        AlertDialog(
            onDismissRequest = { editorOpen = false },
            title = {
                Text(if (editingSnippet == null) "Create snippet" else "Edit snippet")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedTextField(
                        value = trigger,
                        onValueChange = {
                            trigger = it
                            validationError = null
                        },
                        label = { Text("Trigger") },
                        supportingText = {
                            Text("No spaces; ${SnippetManager.MAX_TRIGGER_LENGTH} characters maximum.")
                        },
                        isError = validationError != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = replacement,
                        onValueChange = {
                            replacement = it
                            validationError = null
                        },
                        label = { Text("Replacement text") },
                        isError = validationError != null,
                        minLines = 3,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    validationError?.let { message ->
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val error = snippets.save(
                            originalTrigger = editingSnippet?.trigger,
                            trigger = trigger,
                            text = replacement
                        )
                        if (error == null) {
                            allSnippets = snippets.getAll()
                            editorOpen = false
                        } else {
                            validationError = error
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editorOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SwitchOption(
    label: String,
    value: Boolean,
    textColor: ComposeColor,
    accentColor: ComposeColor,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = textColor, style = AppTypography.bodyMedium)
        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            modifier = Modifier.scale(1.1f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun ToggleOption(
    label: String,
    isSelected: Boolean,
    textColor: ComposeColor,
    accentColor: ComposeColor,
    onToggle: () -> Unit
) {
    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = Shapes.md,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else SemanticColors.getSubtleAccent(accentColor, true),
            contentColor = if (isSelected) ComposeColor.White else textColor
        )
    ) {
        Text(label, style = AppTypography.labelMedium)
    }
}

package com.openswift.keyboard.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.openswift.keyboard.data.CustomizationPackageException
import com.openswift.keyboard.data.CustomizationPackageManager
import com.openswift.keyboard.data.CustomizationPackageParser
import com.openswift.keyboard.data.DataPortability
import com.openswift.keyboard.data.KeyboardLanguages
import com.openswift.keyboard.data.PerAppSettings
import com.openswift.keyboard.engine.UserDictionary
import com.openswift.keyboard.layout.CustomLayoutStore
import com.openswift.keyboard.theme.KbTheme
import com.openswift.keyboard.theme.ThemeEditor
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

class MainActivity : AppCompatActivity() {
    private var pendingImportMode = DataPortability.ImportMode.MERGE

    private val exportData = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching {
            contentResolver.openOutputStream(uri)?.use { stream ->
                stream.writer(Charsets.UTF_8).use { it.write(DataPortability(this).exportJson()) }
            } ?: error("Unable to open export destination")
        }.onSuccess {
            toast("OpenSwift data exported")
        }.onFailure {
            toast("Export failed: ${it.message ?: "unknown error"}")
        }
    }

    private val importData = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching {
            val raw = contentResolver.openInputStream(uri)?.use { stream ->
                stream.reader(Charsets.UTF_8).readText()
            } ?: error("Unable to open import file")
            DataPortability(this).importJson(raw, pendingImportMode)
        }.onSuccess {
            toast(it.asMessage())
        }.onFailure {
            toast("Import failed: ${it.message ?: "unknown error"}")
        }
    }

    private val importCustomization = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching {
            CustomizationPackageManager(this).importJson(readCustomizationPackage(uri))
        }.onSuccess {
            toast(it.asMessage())
            intent.putExtra(EXTRA_OPEN_SETTINGS, true)
            recreate()
        }.onFailure {
            toast("Package import failed: ${it.message ?: "unknown error"}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialPerAppPackage = intent.getStringExtra(EXTRA_PER_APP_PACKAGE).orEmpty()
        val openSettings = intent.getBooleanExtra(EXTRA_OPEN_SETTINGS, false)
        setContent {
            val settings = Settings(this@MainActivity)
            MainUI(
                settings = settings,
                context = this@MainActivity,
                onExportData = { exportData.launch("openswift-data.json") },
                onImportMerge = {
                    pendingImportMode = DataPortability.ImportMode.MERGE
                    importData.launch(arrayOf("application/json", "text/json", "*/*"))
                },
                onImportReplace = {
                    pendingImportMode = DataPortability.ImportMode.REPLACE
                    importData.launch(arrayOf("application/json", "text/json", "*/*"))
                },
                onImportCustomization = {
                    importCustomization.launch(arrayOf("application/json", "text/json", "*/*"))
                },
                initialPerAppPackage = initialPerAppPackage,
                openSettings = openSettings,
            )
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun readCustomizationPackage(uri: android.net.Uri): String {
        val input = contentResolver.openInputStream(uri)
            ?: throw CustomizationPackageException("Unable to open the selected file.")
        val bytes = input.use {
            val result = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = it.read(buffer)
                if (count < 0) break
                result.write(buffer, 0, count)
                if (result.size() > CustomizationPackageParser.MAX_PACKAGE_BYTES) {
                    throw CustomizationPackageException("The package is larger than 512 KiB.")
                }
            }
            result.toByteArray()
        }
        return runCatching {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        }.getOrElse {
            throw CustomizationPackageException("The package is not valid UTF-8 text.")
        }
    }

    companion object {
        const val EXTRA_PER_APP_PACKAGE = "com.openswift.keyboard.extra.PER_APP_PACKAGE"
        private const val EXTRA_OPEN_SETTINGS = "com.openswift.keyboard.extra.OPEN_SETTINGS"
    }
}

@Composable
fun MainUI(
    settings: Settings,
    context: android.content.Context,
    onExportData: () -> Unit = {},
    onImportMerge: () -> Unit = {},
    onImportReplace: () -> Unit = {},
    onImportCustomization: () -> Unit = {},
    initialPerAppPackage: String = "",
    openSettings: Boolean = false,
) {
    var activeTab by remember(initialPerAppPackage, openSettings) {
        mutableStateOf(if (openSettings || initialPerAppPackage.isNotBlank()) 1 else 0)
    }
    val perAppSettings = remember(context) { PerAppSettings(context) }
    val themeEditor = remember(context) { ThemeEditor(context) }
    val availableThemes = remember(themeEditor) { themeEditor.listThemes() }
    val customLayouts = remember(context) { CustomLayoutStore(context).list() }
    
    val theme = themeEditor.resolve(settings.theme)
    val bgColor = Color(theme.background)
    val keyBgColor = Color(theme.keyBackground)
    val textColor = Color(theme.keyText)
    val accentColor = Color(theme.keyAccent)
    val surfaceColor = SemanticColors.getSurfaceColor(theme.background.toLong(), theme.keyBackground.toLong())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .safeDrawingPadding()
    ) {
        // Tab content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (activeTab) {
                0 -> HomeUI(theme, bgColor, keyBgColor, textColor, accentColor)
                1 -> EnhancedSettingsUI(
                    settings,
                    bgColor,
                    textColor,
                    accentColor,
                    onExportData,
                    onImportMerge,
                    onImportReplace,
                    onImportCustomization,
                    perAppSettings,
                    initialPerAppPackage,
                    availableThemes,
                    customLayouts.map { it.id to it.name },
                )
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
fun EnhancedSettingsUI(
    settings: Settings,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    onExportData: () -> Unit = {},
    onImportMerge: () -> Unit = {},
    onImportReplace: () -> Unit = {},
    onImportCustomization: () -> Unit = {},
    perAppSettings: PerAppSettings,
    initialPerAppPackage: String = "",
    availableThemes: List<KbTheme>,
    customLayoutOptions: List<Pair<String, String>>,
) {
    var selectedThemeId by remember { mutableStateOf(settings.theme) }
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
                currentThemeId = selectedThemeId,
                onThemeChange = {
                    settings.theme = it
                    selectedThemeId = it
                },
                themes = availableThemes,
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
                    "Layout" to (
                        listOf(
                            "qwerty" to "QWERTY",
                            "qwertz" to "QWERTZ",
                            "azerty" to "AZERTY",
                        ) + customLayoutOptions
                    ),
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
            ToggleOption("Glide Typing", settings.glideEnabled, textColor) { settings.glideEnabled = it }
            ToggleOption("Auto-Correct", settings.autoCorrect, textColor) { settings.autoCorrect = it }
            ToggleOption("Detect Language", settings.languageDetection, textColor) { settings.languageDetection = it }
            ToggleOption("Auto-Capitalize", settings.autoCapitalize, textColor) { settings.autoCapitalize = it }
        }

        // Feedback
        SettingsSection(
            title = "🔊 Feedback",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Haptic Feedback", settings.hapticFeedback, textColor) { settings.hapticFeedback = it }
            ToggleOption("Sound Effects", settings.soundFeedback, textColor) { settings.soundFeedback = it }
        }

        // Accessibility
        SettingsSection(
            title = "♿ Accessibility",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Reduce Motion", settings.reducedMotion, textColor) { settings.reducedMotion = it }
        }

        // Advanced
        SettingsSection(
            title = "🔧 Advanced",
            textColor = textColor,
            accentColor = accentColor
        ) {
            ToggleOption("Power Saving Mode", settings.powerSaveMode, textColor) { settings.powerSaveMode = it }
            ToggleOption("Clipboard History", settings.clipboardEnabled, textColor) { settings.clipboardEnabled = it }
            ToggleOption("Per-App Tint", settings.perAppTint, textColor) { settings.perAppTint = it }
            ToggleOption("Incognito Mode", settings.incognitoMode, textColor) { settings.incognitoMode = it }
            PerAppProfilesUI(
                profilesStore = perAppSettings,
                initialPackageName = initialPerAppPackage,
                textColor = textColor,
                accentColor = accentColor,
            )
            CustomizationPackageActions(onImportCustomization, textColor)
            DataPortabilityActions(
                accentColor = accentColor,
                textColor = textColor,
                onExportData = onExportData,
                onImportMerge = onImportMerge,
                onImportReplace = onImportReplace
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CustomizationPackageActions(onImportCustomization: () -> Unit, textColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            "Customization Packages",
            style = AppTypography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
        Text(
            "Import a validated OpenSwift JSON package containing custom themes or keyboard layouts.",
            style = AppTypography.bodySmall,
            color = textColor.copy(alpha = Alphas.secondary),
        )
        OutlinedButton(onClick = onImportCustomization) {
            Text("Import theme/layout package")
        }
    }
}

@Composable
fun DataPortabilityActions(
    accentColor: Color,
    textColor: Color,
    onExportData: () -> Unit,
    onImportMerge: () -> Unit,
    onImportReplace: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Text(
            "Data Portability",
            style = AppTypography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Button(
                onClick = onExportData,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Export")
            }
            OutlinedButton(
                onClick = onImportMerge,
                modifier = Modifier.weight(1f)
            ) {
                Text("Merge")
            }
            OutlinedButton(
                onClick = onImportReplace,
                modifier = Modifier.weight(1f)
            ) {
                Text("Replace")
            }
        }
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
fun ToggleOption(
    label: String,
    value: Boolean,
    textColor: Color,
    onToggle: (Boolean) -> Unit,
) {
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
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = value,
            onCheckedChange = onToggle,
            modifier = Modifier.scale(0.95f)
        )
    }
}

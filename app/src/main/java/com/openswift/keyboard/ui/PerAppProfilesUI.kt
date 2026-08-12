package com.openswift.keyboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.openswift.keyboard.data.PerAppSettings
import kotlin.math.roundToInt

@Composable
fun PerAppProfilesUI(
    profilesStore: PerAppSettings,
    initialPackageName: String,
    textColor: Color,
    accentColor: Color,
) {
    val initialConfig = remember(initialPackageName) {
        initialPackageName.takeIf { it.isNotBlank() }
            ?.let(profilesStore::getAppConfig)
            ?: PerAppSettings.AppConfig("")
    }
    var profiles by remember { mutableStateOf(profilesStore.getAllAppConfigs()) }
    var editorOpen by remember(initialPackageName) { mutableStateOf(initialPackageName.isNotBlank()) }
    var originalPackageName by remember(initialPackageName) {
        mutableStateOf(initialConfig.packageName.takeIf { initialConfig.hasOverrides })
    }
    var packageName by remember(initialPackageName) { mutableStateOf(initialConfig.packageName) }
    var predictionsDisabled by remember(initialPackageName) {
        mutableStateOf(initialConfig.predictionsDisabled)
    }
    var glideDisabled by remember(initialPackageName) { mutableStateOf(initialConfig.glideDisabled) }
    var customHeight by remember(initialPackageName) {
        mutableStateOf(initialConfig.keyHeightOverride != null)
    }
    var keyHeight by remember(initialPackageName) {
        mutableStateOf((initialConfig.keyHeightOverride ?: 56).toFloat())
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun openEditor(config: PerAppSettings.AppConfig = PerAppSettings.AppConfig("")) {
        originalPackageName = config.packageName.takeIf { config.hasOverrides }
        packageName = config.packageName
        predictionsDisabled = config.predictionsDisabled
        glideDisabled = config.glideDisabled
        customHeight = config.keyHeightOverride != null
        keyHeight = (config.keyHeightOverride ?: 56).toFloat()
        validationError = null
        editorOpen = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("Per-App Profiles", style = AppTypography.bodyMedium, color = textColor)
        Text(
            "Disable predictions or glide, or change key height for one app. " +
                "Tap the keyboard settings key while using an app to prefill its package name.",
            style = AppTypography.bodySmall,
            color = textColor.copy(alpha = Alphas.secondary),
        )
        Button(onClick = { openEditor() }) {
            Text("Add profile")
        }

        profiles.forEach { profile ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SemanticColors.getSubtleAccent(accentColor, true),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text(profile.packageName, style = AppTypography.labelLarge, color = textColor)
                    Text(
                        profile.summary(),
                        style = AppTypography.bodySmall,
                        color = textColor.copy(alpha = Alphas.secondary),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        OutlinedButton(onClick = { openEditor(profile) }) {
                            Text("Edit")
                        }
                        TextButton(
                            onClick = {
                                profilesStore.removeAppConfig(profile.packageName)
                                profiles = profilesStore.getAllAppConfigs()
                            },
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }

        if (profiles.isNotEmpty()) {
            OutlinedButton(
                onClick = {
                    profilesStore.resetAll()
                    profiles = emptyList()
                },
            ) {
                Text("Reset all profiles")
            }
        }
    }

    if (editorOpen) {
        AlertDialog(
            onDismissRequest = { editorOpen = false },
            title = { Text(if (originalPackageName == null) "Add app profile" else "Edit app profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = packageName,
                        onValueChange = {
                            packageName = it
                            validationError = null
                        },
                        label = { Text("App package name") },
                        placeholder = { Text("com.example.app") },
                        singleLine = true,
                        isError = validationError != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PerAppProfileSwitch(
                        label = "Disable predictions and learning",
                        checked = predictionsDisabled,
                        onCheckedChange = { predictionsDisabled = it },
                    )
                    PerAppProfileSwitch(
                        label = "Disable glide typing",
                        checked = glideDisabled,
                        onCheckedChange = { glideDisabled = it },
                    )
                    PerAppProfileSwitch(
                        label = "Custom key height",
                        checked = customHeight,
                        onCheckedChange = { customHeight = it },
                    )
                    if (customHeight) {
                        Text("${keyHeight.roundToInt()} dp", style = AppTypography.bodySmall)
                        Slider(
                            value = keyHeight,
                            onValueChange = { keyHeight = it },
                            valueRange = PerAppSettings.MIN_KEY_HEIGHT_DP.toFloat()..
                                PerAppSettings.MAX_KEY_HEIGHT_DP.toFloat(),
                            steps = PerAppSettings.MAX_KEY_HEIGHT_DP -
                                PerAppSettings.MIN_KEY_HEIGHT_DP - 1,
                        )
                    }
                    if (!predictionsDisabled && !glideDisabled && !customHeight) {
                        Text(
                            "No overrides selected; saving resets this app to global settings.",
                            style = AppTypography.bodySmall,
                        )
                    }
                    validationError?.let { error ->
                        Text(error, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val config = PerAppSettings.AppConfig(
                            packageName = packageName,
                            predictionsDisabled = predictionsDisabled,
                            glideDisabled = glideDisabled,
                            keyHeightOverride = keyHeight.roundToInt().takeIf { customHeight },
                        )
                        val error = profilesStore.saveAppConfig(config)
                        if (error == null) {
                            originalPackageName
                                ?.takeIf { it != packageName.trim() }
                                ?.let(profilesStore::removeAppConfig)
                            profiles = profilesStore.getAllAppConfigs()
                            editorOpen = false
                        } else {
                            validationError = error
                        }
                    },
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editorOpen = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun PerAppProfileSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = AppTypography.bodySmall, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun PerAppSettings.AppConfig.summary(): String = buildList {
    if (predictionsDisabled) add("Predictions and learning off")
    if (glideDisabled) add("Glide off")
    keyHeightOverride?.let { add("$it dp keys") }
}.joinToString(" · ")

package com.openswift.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.data.TypedDataStores
import com.openswift.keyboard.engine.UserDictionary

@Composable
fun PrivacyUI(
    clipboardHistory: ClipboardHistory,
    userDict: UserDictionary,
    bgColor: Color,
    textColor: Color,
    accentColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var clipboardItems by remember { mutableStateOf(clipboardHistory.items()) }
    var wordCount by remember { mutableStateOf(userDict.getWordCount()) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showDeleteAllConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
    ) {
        // Header
        Text(
            "Privacy & Data",
            style = AppTypography.displayMedium,
            color = textColor,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        
        Text(
            "See what OpenSwift stores, reset it, or remove it. Everything here stays on your device.",
            style = AppTypography.bodyMedium,
            color = textColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = Spacing.lg)
        )

        // Clipboard History Section
        PrivacyDataCard(
            icon = Icons.AutoMirrored.Filled.List,
            title = "Clipboard History",
            subtitle = "${clipboardItems.size} items saved",
            bgColor = bgColor,
            accentColor = accentColor,
            textColor = textColor
        ) {
            if (clipboardItems.isEmpty()) {
                Text(
                    "No clipboard items yet. When clipboard history is enabled, copied text will appear here.",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(Spacing.md)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    clipboardItems.take(5).forEach { item ->
                        ClipboardItemRow(item, textColor)
                    }
                    if (clipboardItems.size > 5) {
                        Text(
                            "...and ${clipboardItems.size - 5} more",
                            style = AppTypography.labelSmall,
                            color = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = Spacing.md)
                        )
                    }
                }
            }
            
            Button(
                onClick = { showClearConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                shape = Shapes.sm
            ) {
                Text("Clear History", style = AppTypography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Dictionary Stats Section
        PrivacyDataCard(
            icon = Icons.Filled.Edit,
            title = "Learning & Dictionary",
            subtitle = "$wordCount words learned",
            bgColor = bgColor,
            accentColor = accentColor,
            textColor = textColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                StatsRow("Words Learned", wordCount.toString(), textColor)
                HorizontalDivider(
                    color = textColor.copy(alpha = Alphas.divider),
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
                Text(
                    "Your device learns from your typing patterns to provide better predictions.",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            
            Button(
                onClick = {
                    userDict.reset()
                    wordCount = 0
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.copy(alpha = 0.15f),
                    contentColor = accentColor
                ),
                shape = Shapes.sm
            ) {
                Text("Reset Dictionary", style = AppTypography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Data Deletion Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.md,
            colors = CardDefaults.cardColors(
                containerColor = SemanticColors.getSubtleAccent(Color(0xFFF44336), true)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                    )
                    Text(
                        "Delete all data",
                        style = AppTypography.headlineSmall,
                        color = Color(0xFFC62828),
                    )
                }
                
                Text(
                    "Permanently delete clipboard history, learned words, snippets, custom themes and layouts, emoji history, per-app profiles, and local usage data. This action cannot be undone.",
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )
                
                Button(
                    onClick = { showDeleteAllConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = Shapes.sm
                ) {
                    Text("Delete All Data", style = AppTypography.labelMedium, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        // Privacy Policy Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.05f), Shapes.sm)
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                "OpenSwift has no network permission. Your typing data stays on this device.",
                style = AppTypography.labelSmall,
                color = textColor.copy(alpha = 0.68f),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
    }

    // Confirmation dialogs
    if (showClearConfirmation) {
        ConfirmationDialog(
            title = "Clear Clipboard History?",
            message = "This will delete all ${clipboardItems.size} items. This cannot be undone.",
            onConfirm = {
                clipboardHistory.clear()
                clipboardItems = emptyList()
                showClearConfirmation = false
            },
            onDismiss = { showClearConfirmation = false },
            accentColor = accentColor,
            textColor = textColor,
            bgColor = bgColor
        )
    }

    if (showDeleteAllConfirmation) {
        ConfirmationDialog(
            title = "Delete All Data?",
            message = "This will permanently delete all locally stored typing and customization data.",
            onConfirm = {
                clipboardHistory.clear()
                userDict.reset()
                TypedDataStores.clearAll(context)
                clipboardItems = emptyList()
                wordCount = 0
                showDeleteAllConfirmation = false
            },
            onDismiss = { showDeleteAllConfirmation = false },
            accentColor = Color(0xFFF44336),
            textColor = textColor,
            bgColor = bgColor,
            isDangerous = true
        )
    }
}

@Composable
fun PrivacyDataCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    accentColor: Color,
    textColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shapes.md,
        colors = CardDefaults.cardColors(
            containerColor = SemanticColors.getSubtleAccent(accentColor, true)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = AppTypography.headlineSmall, color = textColor)
                    Text(subtitle, style = AppTypography.labelMedium, color = textColor.copy(alpha = 0.7f))
                }
            }
            
            HorizontalDivider(
                color = textColor.copy(alpha = Alphas.divider),
                modifier = Modifier.fillMaxWidth()
            )
            
            content()
        }
    }
}

@Composable
fun ClipboardItemRow(item: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "•",
            style = AppTypography.bodyMedium,
            color = textColor.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = Spacing.sm)
        )
        Text(
            item.take(50) + if (item.length > 50) "…" else "",
            style = AppTypography.bodySmall,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AppTypography.bodyMedium, color = textColor)
        Text(value, style = AppTypography.labelLarge, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    accentColor: Color,
    textColor: Color,
    bgColor: Color,
    isDangerous: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = AppTypography.headlineSmall, color = textColor)
        },
        text = {
            Text(message, style = AppTypography.bodyMedium, color = textColor.copy(alpha = 0.8f))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDangerous) Color(0xFFF44336) else accentColor
                ),
                shape = Shapes.sm
            ) {
                Text("Confirm", style = AppTypography.labelMedium, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = Shapes.sm
            ) {
                Text("Cancel", style = AppTypography.labelMedium)
            }
        },
        containerColor = bgColor,
        shape = Shapes.md
    )
}

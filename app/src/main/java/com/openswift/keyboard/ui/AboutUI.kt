package com.openswift.keyboard.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutUI(
    bgColor: Color,
    textColor: Color,
    accentColor: Color
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            "⌨️",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = Spacing.md)
        )
        
        Text(
            "OpenSwift",
            style = AppTypography.displayLarge,
            color = textColor
        )
        
        Text(
            "v0.3.1",
            style = AppTypography.bodyMedium,
            color = textColor.copy(alpha = 0.7f)
        )
        
        Divider(
            color = accentColor.copy(alpha = Alphas.divider),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.lg)
        )
        
        // About section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.md,
            colors = CardDefaults.cardColors(
                containerColor = SemanticColors.getSubtleAccent(accentColor, true)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    "About",
                    style = AppTypography.headlineSmall,
                    color = textColor
                )
                
                Text(
                    "OpenSwift is a modern, fast, and customizable Android keyboard with glide typing, intelligent predictions, beautiful themes, and privacy at its core.",
                    style = AppTypography.bodyMedium,
                    color = textColor.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
            }
        }
        
        // Features checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.md,
            colors = CardDefaults.cardColors(
                containerColor = SemanticColors.getSubtleAccent(accentColor, true)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    "Key Features",
                    style = AppTypography.headlineSmall,
                    color = textColor
                )
                
                listOf(
                    "Glide typing with smart gesture detection",
                    "10 beautiful color themes including accessibility modes",
                    "Animated ripple & glide trail effects",
                    "English, German, French, and Spanish dictionaries",
                    "Emoji keyboard with quick access",
                    "Smart predictions & auto-correct",
                    "Privacy dashboard & clipboard management",
                    "Reduced motion mode for accessibility",
                    "No telemetry—100% private"
                ).forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✓", color = accentColor, style = AppTypography.labelLarge)
                        Text(
                            feature,
                            style = AppTypography.bodySmall,
                            color = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Links
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            LinkButton(
                label = "GitHub Repository",
                url = "https://github.com/SysAdminDoc/OpenSwift",
                accentColor = accentColor,
                context = context
            )
            
            LinkButton(
                label = "Report an Issue",
                url = "https://github.com/SysAdminDoc/OpenSwift/issues",
                accentColor = accentColor,
                context = context
            )
            
            LinkButton(
                label = "View Documentation",
                url = "https://github.com/SysAdminDoc/OpenSwift#readme",
                accentColor = accentColor,
                context = context
            )
        }
        
        // Footer
        Text(
            "Made with ❤️ for Android users",
            style = AppTypography.labelMedium,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = Spacing.xl)
        )
    }
}

@Composable
fun LinkButton(
    label: String,
    url: String,
    accentColor: Color,
    context: android.content.Context
) {
    OutlinedButton(
        onClick = {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = Shapes.md,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Text(label, style = AppTypography.labelLarge, color = accentColor)
    }
}

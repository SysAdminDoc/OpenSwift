package com.openswift.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = Shapes.md,
        colors = CardDefaults.cardColors(
            containerColor = SemanticColors.getSubtleAccent(accentColor, true)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevations.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor.copy(alpha = 0.2f), Shapes.md),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    title,
                    style = AppTypography.labelLarge,
                    color = textColor
                )
                Text(
                    description,
                    style = AppTypography.bodySmall,
                    color = textColor.copy(alpha = 0.72f),
                    maxLines = 2
                )
            }
        }
    }
}

package com.openswift.keyboard.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.theme.KbTheme
import com.openswift.keyboard.theme.Themes

@Composable
fun ColorCustomizer(
    currentThemeId: String,
    onThemeChange: (String) -> Unit,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            "Color Schemes",
            style = AppTypography.headlineSmall,
            color = textColor
        )
        
        Themes.all.forEach { theme ->
            ColorOption(
                theme = theme,
                isSelected = currentThemeId == theme.id,
                onSelect = { onThemeChange(theme.id) },
                textColor = textColor,
                accentColor = accentColor
            )
        }
    }
}

@Composable
fun ColorOption(
    theme: KbTheme,
    isSelected: Boolean,
    onSelect: () -> Unit,
    textColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = Shapes.md,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(theme.keyAccent).copy(alpha = 0.12f)
            } else {
                SemanticColors.getSubtleAccent(Color(theme.keyAccent), true)
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(theme.keyAccent))
        } else {
            BorderStroke(1.dp, Color(theme.keyAccent).copy(alpha = 0.3f))
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) Elevations.md else Elevations.sm
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview boxes
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(theme.background), Shapes.xs)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(theme.keyBackground), Shapes.xs)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(theme.keyText), Shapes.xs)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(theme.keyAccent), Shapes.xs)
                )
            }
            
            // Theme name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    theme.name,
                    style = AppTypography.labelLarge,
                    color = textColor
                )
            }
            
            // Checkmark if selected
            if (isSelected) {
                Text(
                    "✓",
                    color = Color(theme.keyAccent),
                    style = AppTypography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

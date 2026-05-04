package com.openswift.keyboard.ui

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
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Color Schemes",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Themes.all.forEach { theme ->
            ColorOption(
                theme = theme,
                isSelected = currentThemeId == theme.id,
                onSelect = { onThemeChange(theme.id) },
                textColor = textColor
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(theme.keyAccent).copy(alpha = 0.2f) else Color.Transparent
        ),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview boxes
            Row(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(theme.background), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(theme.keyBackground), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(theme.keyText), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(theme.keyAccent), RoundedCornerShape(4.dp))
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    theme.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            
            if (isSelected) {
                Text(
                    "✓",
                    fontSize = 20.sp,
                    color = Color(theme.keyAccent),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

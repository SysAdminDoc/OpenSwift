package com.openswift.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.theme.KbTheme

@Composable
fun KeyboardPreview(theme: KbTheme, modifier: Modifier = Modifier) {
    val bgColor = Color(theme.keyBackground)
    val textColor = Color(theme.keyText)
    val accentColor = Color(theme.keyAccent)
    val keySpacing = 4.dp
    val keyHeight = 36.dp
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(theme.background), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(keySpacing)
    ) {
        // Row 1: qwerty...
        KeyboardRow(
            keys = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            bgColor = bgColor,
            textColor = textColor,
            keyHeight = keyHeight,
            keySpacing = keySpacing
        )
        
        // Row 2: asdf...
        KeyboardRow(
            keys = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            bgColor = bgColor,
            textColor = textColor,
            keyHeight = keyHeight,
            keySpacing = keySpacing,
            startPadding = 12.dp
        )
        
        // Row 3: shift zxcv... delete
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviewKey("⇧", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.2f), isModifier = true)
            PreviewKey("z", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("x", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("c", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("v", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("b", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("n", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("m", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("⌫", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.2f), isModifier = true)
        }
        
        // Row 4: Space bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(keyHeight),
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviewKey("123", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f), isModifier = true)
            PreviewKey("", bgColor, textColor, keyHeight, modifier = Modifier.weight(5f))
            PreviewKey(".", bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
            PreviewKey("⏎", bgColor, textColor, keyHeight, modifier = Modifier.weight(1.2f), isModifier = true)
        }
    }
}

@Composable
fun KeyboardRow(
    keys: List<String>,
    bgColor: Color,
    textColor: Color,
    keyHeight: androidx.compose.ui.unit.Dp,
    keySpacing: androidx.compose.ui.unit.Dp,
    startPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyHeight)
            .padding(start = startPadding),
        horizontalArrangement = Arrangement.spacedBy(keySpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        keys.forEach { key ->
            PreviewKey(key, bgColor, textColor, keyHeight, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PreviewKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    isModifier: Boolean = false
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                if (isModifier) bgColor.copy(alpha = 0.7f) else bgColor,
                RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = textColor,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

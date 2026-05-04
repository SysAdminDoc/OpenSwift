package com.openswift.keyboard.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Premium design system for OpenSwift.
 * Provides consistent spacing, typography, shapes, and interaction patterns.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Shapes {
    val xs = RoundedCornerShape(4.dp)
    val sm = RoundedCornerShape(8.dp)
    val md = RoundedCornerShape(12.dp)
    val lg = RoundedCornerShape(16.dp)
}

object Elevations {
    val none = 0.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
}

/**
 * Typography scale following Material 3 with premium refinements
 */
object AppTypography {
    // Display/Hero
    val displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    )
    
    val displayMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    )
    
    // Headings
    val headlineLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )
    
    val headlineMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )
    
    val headlineSmall = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )
    
    // Labels
    val labelLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )
    
    val labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
    
    val labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    
    // Body
    val bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    
    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    
    val bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    )
}

/**
 * Semantic color utilities
 */
object SemanticColors {
    fun getSurfaceColor(bgColor: Long, keyBgColor: Long): Color {
        // Slightly elevated surface level
        val bg = Color(bgColor)
        val keyBg = Color(keyBgColor)
        // Average between bg and keyBg for midtone surface
        return Color(
            red = (bg.red + keyBg.red) / 2,
            green = (bg.green + keyBg.green) / 2,
            blue = (bg.blue + keyBg.blue) / 2,
            alpha = 1f
        )
    }
    
    fun getSubtleAccent(accentColor: Color, isDark: Boolean): Color {
        return if (isDark) {
            accentColor.copy(alpha = 0.15f)
        } else {
            accentColor.copy(alpha = 0.10f)
        }
    }
    
    fun getSuccessColor(): Color = Color(0xFF4CAF50)
    fun getWarningColor(): Color = Color(0xFFFFC107)
    fun getErrorColor(): Color = Color(0xFFF44336)
    fun getInfoColor(): Color = Color(0xFF2196F3)
}

/**
 * Opacity values for semantic meaning
 */
object Alphas {
    val disabled = 0.38f
    val secondary = 0.6f
    val hint = 0.5f
    val divider = 0.12f
}

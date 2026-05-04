package com.openswift.keyboard.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.theme.KbTheme

@Composable
fun HomeUI(
    theme: KbTheme,
    bgColor: Color,
    keyBgColor: Color,
    textColor: Color,
    accentColor: Color
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f),
                            bgColor
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    "⌨️",
                    fontSize = 56.sp
                )
                Text(
                    "OpenSwift",
                    style = AppTypography.displayMedium,
                    color = textColor
                )
                Text(
                    "Professional Android keyboard",
                    style = AppTypography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            // Keyboard Preview
            Text(
                "Preview",
                style = AppTypography.headlineSmall,
                color = textColor,
                modifier = Modifier.padding(start = Spacing.sm)
            )
            KeyboardPreview(theme, modifier = Modifier.fillMaxWidth())
            
            // Feature Highlights
            Text(
                "Features",
                style = AppTypography.headlineSmall,
                color = textColor,
                modifier = Modifier.padding(start = Spacing.sm)
            )
            
            FeatureCard(
                icon = "👆",
                title = "Glide Typing",
                description = "Swipe across keys for fast, accurate input",
                accentColor = accentColor
            )
            
            FeatureCard(
                icon = "🎨",
                title = "Beautiful Themes",
                description = "10 stunning color schemes to choose from",
                accentColor = accentColor
            )
            
            FeatureCard(
                icon = "😊",
                title = "Emoji Support",
                description = "Quick access to thousands of emoji",
                accentColor = accentColor
            )
            
            FeatureCard(
                icon = "⚡",
                title = "Lightning Fast",
                description = "Optimized for speed with smart predictions",
                accentColor = accentColor
            )
            
            // Call to Action
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                shape = Shapes.md
            ) {
                Text(
                    "Enable Keyboard",
                    style = AppTypography.labelLarge,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedButton(
                onClick = { /* Scroll to settings tab */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Text(
                    "Browse Themes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

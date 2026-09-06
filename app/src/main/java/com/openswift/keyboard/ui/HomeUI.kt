package com.openswift.keyboard.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.openswift.keyboard.R
import com.openswift.keyboard.theme.KbTheme

@Composable
fun HomeUI(
    theme: KbTheme,
    bgColor: Color,
    textColor: Color,
    accentColor: Color,
    onBrowseThemes: () -> Unit,
) {
    val context = LocalContext.current
    val buttonTextColor = if (accentColor.luminance() > 0.54f) Color.Black else Color.White
    
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
                Image(
                    painter = painterResource(R.drawable.openswift_brand),
                    contentDescription = "OpenSwift app icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(78.dp)
                )
                Text(
                    "OpenSwift",
                    style = AppTypography.displayMedium,
                    color = textColor
                )
                Text(
                    "Type faster. Keep every keystroke private.",
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
            
            Text(
                "Built for private typing",
                style = AppTypography.headlineSmall,
                color = textColor,
                modifier = Modifier.padding(start = Spacing.sm)
            )
            
            FeatureCard(
                icon = Icons.AutoMirrored.Filled.Send,
                title = "Glide with confidence",
                description = "Trace words across the keys. Decoding runs offline.",
                accentColor = accentColor,
                textColor = textColor,
            )
            
            FeatureCard(
                icon = Icons.Filled.Lock,
                title = "Private by design",
                description = "No network permission, telemetry, ads, or account.",
                accentColor = accentColor,
                textColor = textColor,
            )
            
            FeatureCard(
                icon = Icons.Filled.Star,
                title = "Built around you",
                description = "Ten themes, three layouts, and per-app profiles.",
                accentColor = accentColor,
                textColor = textColor,
            )
            
            FeatureCard(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Five offline languages",
                description = "English, German, French, Spanish, and Italian.",
                accentColor = accentColor,
                textColor = textColor,
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
                    containerColor = accentColor,
                    contentColor = buttonTextColor,
                ),
                shape = Shapes.md
            ) {
                Text(
                    "Enable in Android settings",
                    style = AppTypography.labelLarge,
                    color = buttonTextColor,
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            OutlinedButton(
                onClick = onBrowseThemes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = Shapes.md,
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
            ) {
                Text(
                    "Explore themes and settings",
                    style = AppTypography.labelLarge,
                    color = accentColor,
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

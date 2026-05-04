package com.openswift.keyboard.ui

import android.content.Intent
import android.net.Uri
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "⌨️",
            fontSize = 56.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            "OpenSwift",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        
        Text(
            "v0.1.0",
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.7f)
        )
        
        Divider(
            color = accentColor.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        // About section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "About",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                "OpenSwift is a modern, fast, and customizable Android keyboard with glide typing, multiple themes, emoji support, and advanced prediction features.",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
        
        // Features checklist
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Features",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            listOf(
                "Glide typing with smart gesture detection",
                "8+ beautiful color themes",
                "Emoji keyboard with quick access",
                "Word predictions & auto-correct",
                "Multiple keyboard layouts (QWERTY, QWERTZ, AZERTY)",
                "Customizable key height & spacing",
                "Haptic feedback & sound effects",
                "Privacy-focused (no telemetry)"
            ).forEach { feature ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        feature,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.8f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Links
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                url = "https://github.com/SysAdminDoc/OpenSwift/blob/master/GUIDE.md",
                accentColor = accentColor,
                context = context
            )
        }
        
        // Footer
        Text(
            "Made with ❤️ for Android users",
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 24.dp)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, modifier = Modifier.padding(8.dp))
    }
}

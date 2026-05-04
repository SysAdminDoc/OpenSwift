package com.openswift.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openswift.keyboard.data.ClipboardHistory
import com.openswift.keyboard.engine.UserDictionary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrivacyUI(
    clipboardHistory: ClipboardHistory,
    userDict: UserDictionary,
    bgColor: Color,
    textColor: Color,
    accentColor: Color
) {
    var clipboardItems by remember { mutableStateOf(clipboardHistory.items()) }
    var wordCount by remember { mutableStateOf(userDict.getWordCount()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Clipboard History Section
        PrivacySectionHeader("📋 Clipboard History", textColor, accentColor)

        if (clipboardItems.isEmpty()) {
            Text(
                "No clipboard items yet",
                color = textColor.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            clipboardItems.take(10).forEach { item ->
                ClipboardItemRow(item, textColor)
            }
            if (clipboardItems.size > 10) {
                Text(
                    "...and ${clipboardItems.size - 10} more items",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Button(
            onClick = {
                clipboardHistory.clear()
                clipboardItems = emptyList()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Clear Clipboard", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dictionary Stats Section
        PrivacySectionHeader("📚 Dictionary & Learning", textColor, accentColor)

        StatsRow("Words Learned", wordCount.toString(), textColor)

        Button(
            onClick = {
                userDict.reset()
                wordCount = 0
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Reset Dictionary", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Deletion Section
        PrivacySectionHeader("⚠️ Delete All Data", textColor, accentColor)

        Text(
            "This will clear clipboard history and reset your learned dictionary. This action cannot be undone.",
            color = textColor.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                clipboardHistory.clear()
                userDict.reset()
                clipboardItems = emptyList()
                wordCount = 0
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
        ) {
            Text("Delete All Data", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Privacy Policy
        Text(
            "🔒 All data is stored locally on your device. No data is sent to external servers.",
            color = textColor.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun PrivacySectionHeader(title: String, textColor: Color, accentColor: Color) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun ClipboardItemRow(item: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            item.take(50) + if (item.length > 50) "..." else "",
            color = textColor,
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatsRow(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = textColor, fontSize = 14.sp)
        Text(value, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

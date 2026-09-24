package com.example.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.model.ArchiveTestResult
import com.example.model.formatFileSize

@Composable
fun ArchiveTestResultModal(
    archiveName: String,
    result: ArchiveTestResult,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (result.isValid) Color(0xFF1B3B2B) else Color(0xFF4A1818)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Status",
                        tint = if (result.isValid) Color(0xFF00C853) else Color(0xFFFF5252),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (result.isValid) {
                        if (language == AppLanguage.HINDI) "7-Zip टेस्ट सफल रहा (No Errors)" else "7-Zip Integrity Test Passed"
                    } else {
                        if (language == AppLanguage.HINDI) "आर्काइव टेस्ट में त्रुटि पाई गई" else "Archive Test Failed"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    text = archiveName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Surface
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        MetricRow(
                            label = if (language == AppLanguage.HINDI) "टेस्ट की गई फ़ाइलें" else "Files Tested",
                            value = "${result.filesTested} files"
                        )
                        MetricRow(
                            label = if (language == AppLanguage.HINDI) "सत्यापित डेटा" else "Verified Data",
                            value = formatFileSize(result.totalUncompressedBytes)
                        )
                        MetricRow(
                            label = if (language == AppLanguage.HINDI) "टेस्ट समय (Duration)" else "Test Duration",
                            value = "${result.durationMs} ms"
                        )
                        MetricRow(
                            label = if (language == AppLanguage.HINDI) "CRC32 अखंडता स्थिति" else "CRC32 Status",
                            value = if (result.isValid) "EVERYTHING OK" else "ERRORS FOUND",
                            valueColor = if (result.isValid) Color(0xFF00C853) else Color(0xFFFF5252)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Verification Log List
                Text(
                    text = if (language == AppLanguage.HINDI) "CRC-32 सत्यापन लॉग:" else "CRC-32 Verification Log:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(result.details) { log ->
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (log.contains("[OK]")) Color(0xFF00C853) else Color(0xFFFF5252)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (language == AppLanguage.HINDI) "ठीक है (OK)" else "Close (OK)")
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

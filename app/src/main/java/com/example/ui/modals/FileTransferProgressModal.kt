package com.example.ui.modals

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*

@Composable
fun FileTransferProgressModal(
    progressState: TransferProgressState,
    language: AppLanguage,
    onSpeedMultiplierChange: (Int) -> Unit,
    onCancelTransfer: () -> Unit
) {
    if (!progressState.isTransferring && progressState.progress < 1.0f) return

    val animatedProgress by animateFloatAsState(
        targetValue = progressState.progress,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "transfer_progress"
    )

    val actionName = if (progressState.action == ClipboardOperationType.MOVE) {
        if (language == AppLanguage.HINDI) "स्थानांतरित (Move)" else "Moving Files"
    } else {
        if (language == AppLanguage.HINDI) "कॉपी (Copying)" else "Copying Files"
    }

    Dialog(
        onDismissRequest = { /* Prevent dismiss during active transfer */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (progressState.speedMultiplier >= 5) Color(0xFFFF6D00)
                                    else MaterialTheme.colorScheme.primaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (progressState.speedMultiplier >= 5) Icons.Default.ElectricBolt else Icons.Default.Speed,
                                contentDescription = "Speed Icon",
                                tint = if (progressState.speedMultiplier >= 5) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = actionName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI)
                                    "फ़ाइल ${progressState.currentFileIndex} का ${progressState.totalFilesCount}"
                                else
                                    "File ${progressState.currentFileIndex} of ${progressState.totalFilesCount}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (progressState.speedMultiplier >= 5) Color(0xFFFF3D00) else Color(0xFF00C853)
                    ) {
                        Text(
                            text = "${progressState.speedMultiplier}x Speed",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Live Speedometer Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = if (progressState.speedMultiplier >= 5) listOf(
                                    Color(0xFF2A1500),
                                    Color(0xFF5D2000),
                                    Color(0xFF8C3000)
                                ) else listOf(
                                    Color(0xFF0A2E1D),
                                    Color(0xFF135034),
                                    Color(0xFF1B6C48)
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = progressState.speedFormatted.substringBefore(" "),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MB/s",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (progressState.speedMultiplier >= 5) Color(0xFFFFD54F) else Color(0xFF69F0AE),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Text(
                            text = if (progressState.speedMultiplier >= 5)
                                (if (language == AppLanguage.HINDI) "⚡ टर्बो स्पीड चालू है! (Maximum Throughput)" else "⚡ TURBO BOOST ACTIVE!")
                            else
                                (if (language == AppLanguage.HINDI) "सामान्य स्थानांतरण गति (Transfer Speed)" else "Current Transfer Speed"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Speed Booster Controls (1x, 2x, 5x, 10x Turbo)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "स्पीड बूस्टर (Boost Speed):" else "Speed Booster:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "गति बढ़ाएँ ⚡" else "Boost Speed ⚡",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 5, 10).forEach { mult ->
                            val isSelected = progressState.speedMultiplier == mult
                            val chipColor = when (mult) {
                                10 -> if (isSelected) Color(0xFFFF3D00) else MaterialTheme.colorScheme.surface
                                5 -> if (isSelected) Color(0xFFFF9100) else MaterialTheme.colorScheme.surface
                                else -> if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = chipColor,
                                border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSpeedMultiplierChange(mult) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (mult == 10) "⚡ 10x Turbo" else "${mult}x",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Current File Info & Size Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressState.currentFileName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = if (progressState.speedMultiplier >= 5) Color(0xFFFF6D00) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detailed Size and Time Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val transferredFormatted = formatFileSize(progressState.bytesTransferred)
                    val totalFormatted = formatFileSize(progressState.totalBytesToTransfer)

                    Text(
                        text = "$transferredFormatted / $totalFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val etaText = if (progressState.estimatedTimeRemainingSec > 0) {
                        if (language == AppLanguage.HINDI)
                            "शेष समय: ${progressState.estimatedTimeRemainingSec} सेकंड"
                        else
                            "ETA: ${progressState.estimatedTimeRemainingSec} sec"
                    } else {
                        if (language == AppLanguage.HINDI) "समाप्त होने वाला है..." else "Finishing..."
                    }

                    Text(
                        text = etaText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancelTransfer,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.HINDI) "रद्द करें (Cancel)" else "Cancel")
                    }

                    if (progressState.speedMultiplier < 10) {
                        Button(
                            onClick = { onSpeedMultiplierChange(10) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF3D00),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "⚡ टर्बो 10x स्पीड" else "⚡ Turbo 10x Speed",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF00C853).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "⚡ अधिकतम गति पर है" else "⚡ Running at MAX Speed",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

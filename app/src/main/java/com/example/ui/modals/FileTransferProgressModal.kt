package com.example.ui.modals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    onSpeedMbpsChange: (Int) -> Unit,
    onPauseToggle: () -> Unit,
    onCancelTransfer: () -> Unit,
    onDismissDone: () -> Unit
) {
    if (!progressState.isTransferring && progressState.progress < 1.0f) return

    val isCompleted = progressState.progress >= 1.0f

    var showSpeedSettings by remember { mutableStateOf(false) }
    var customSpeedInput by remember { mutableStateOf("${progressState.targetMbps}") }

    val animatedProgress by animateFloatAsState(
        targetValue = progressState.progress,
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
        label = "transfer_progress"
    )

    val actionName = if (progressState.action == ClipboardOperationType.MOVE) {
        if (language == AppLanguage.HINDI) "फ़ाइलें स्थानांतरित हो रही हैं... (${progressState.currentFileIndex})" else "Moving Files... (${progressState.currentFileIndex})"
    } else {
        if (language == AppLanguage.HINDI) "फ़ाइलें कॉपी हो रही हैं... (${progressState.currentFileIndex})" else "Copying Files... (${progressState.currentFileIndex})"
    }

    Dialog(
        onDismissRequest = { if (isCompleted) onDismissDone() },
        properties = DialogProperties(dismissOnBackPress = isCompleted, dismissOnClickOutside = isCompleted)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Video design)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.primary
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isCompleted) {
                                    if (language == AppLanguage.HINDI) "फ़ाइल सफलतापूर्वक ट्रांसफर हो गई" else "File Transferred Successfully"
                                } else actionName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${progressState.sourceLocationName} ➔ ${progressState.destinationLocationName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isCompleted) onDismissDone() else onCancelTransfer()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Speed Banner Card (Dark Container matching video 00:20 - 00:45)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF12141A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showSpeedSettings = !showSpeedSettings }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "ट्रांसफर स्पीड" else "Transfer Speed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!isCompleted) {
                                    IconButton(
                                        onClick = onPauseToggle,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (progressState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                            contentDescription = "Pause",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "⚡ ${progressState.targetMbps} MB/s",
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live Speed Digits
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = if (isCompleted) "0.0" else progressState.speedFormatted.substringBefore(" "),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MB/s",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64B5F6),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Waveform Equalizer Bars (Video design element)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val barHeights = listOf(0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 1.0f, 0.6f, 0.8f, 0.4f, 0.9f, 0.5f, 0.7f, 0.3f, 0.8f, 0.6f, 0.9f, 0.4f)
                            barHeights.forEachIndexed { idx, hFactor ->
                                val activeHeight = if (isCompleted || progressState.isPaused) 0.2f else hFactor
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 1.dp)
                                        .fillMaxHeight(activeHeight)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }

                // Expandable Custom Speed Settings Panel (Video timestamps 00:25 - 00:30)
                AnimatedVisibility(visible = showSpeedSettings && !isCompleted) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "स्पीड सेटिंग" else "Speed Settings",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "Kitna MBPS speed lena hai select karein" else "Select desired transfer speed in MBPS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                25 to "25 MB/s",
                                60 to "60 MB/s",
                                120 to "120 MB/s"
                            ).forEach { (speedVal, labelText) ->
                                val isSel = progressState.targetMbps == speedVal
                                Button(
                                    onClick = {
                                        onSpeedMbpsChange(speedVal)
                                        customSpeedInput = "$speedVal"
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Text(labelText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Numeric Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customSpeedInput,
                                onValueChange = { customSpeedInput = it.take(4) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text("Custom MBPS", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val customVal = customSpeedInput.toIntOrNull() ?: 35
                                    onSpeedMbpsChange(customVal.coerceIn(5, 500))
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (language == AppLanguage.HINDI) "Set MBPS" else "Set MBPS", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current File Info & Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressState.currentFileName.ifEmpty { "File" },
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
                        color = if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Linear Progress Indicator
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Transferred Bytes & Time Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val transferredFormatted = formatFileSize(progressState.bytesTransferred)
                    val totalFormatted = formatFileSize(progressState.totalBytesToTransfer)

                    Text(
                        text = "$transferredFormatted of $totalFormatted (${progressState.currentFileIndex}/${progressState.totalFilesCount} files)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val etaText = if (isCompleted) {
                        if (language == AppLanguage.HINDI) "पूर्ण हुआ" else "Completed"
                    } else if (progressState.estimatedTimeRemainingSec > 0) {
                        "~${progressState.estimatedTimeRemainingSec}s ${if (language == AppLanguage.HINDI) "शेष समय" else "left"}"
                    } else {
                        "~1s"
                    }

                    Text(
                        text = etaText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isCompleted) Color(0xFF00C853) else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Storage Direction Card (From -> To)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("From:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(progressState.sourceLocationName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SdCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF9C27B0))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("To:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(progressState.destinationLocationName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Button
                if (isCompleted) {
                    Button(
                        onClick = onDismissDone,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "हो गया (Done)" else "Done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onCancelTransfer,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

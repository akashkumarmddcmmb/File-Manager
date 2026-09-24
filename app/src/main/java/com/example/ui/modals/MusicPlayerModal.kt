package com.example.ui.modals

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AppLanguage
import com.example.model.FileItem
import com.example.model.PlaybackRepeatMode

@Composable
fun MusicPlayerModal(
    playingFile: FileItem,
    isPlaying: Boolean,
    positionSeconds: Int,
    durationSeconds: Int,
    language: AppLanguage,
    isShuffle: Boolean = false,
    repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.REPEAT_ALL,
    playbackSpeed: Float = 1.0f,
    equalizerPreset: String = "Flat",
    activeSleepTimerMinutes: Int = 0,
    onTogglePlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Int) -> Unit,
    onToggleShuffle: () -> Unit = {},
    onCycleRepeatMode: () -> Unit = {},
    onSetSpeed: (Float) -> Unit = {},
    onSetEqualizer: (String) -> Unit = {},
    onSetSleepTimer: (Int) -> Unit = {},
    onToggleStar: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var isStarredState by remember(playingFile.id, playingFile.isStarred) { mutableStateOf(playingFile.isStarred) }
    var showSleepTimerSelector by remember { mutableStateOf(false) }
    var showEqualizerSelector by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSpeedSelector by remember { mutableStateOf(false) }
    var showInfoModal by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "music_vinyl_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF101318)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "अब चल रहा है" else "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Files by Akash Kumar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            isStarredState = !isStarredState
                            onToggleStar(playingFile.id)
                        }) {
                            Icon(
                                imageVector = if (isStarredState) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Star / Favorite",
                                tint = if (isStarredState) Color(0xFFF9AB00) else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Options",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier
                                    .background(Color(0xFF222630))
                                    .width(220.dp)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = if (language == AppLanguage.HINDI) "प्लेबैक स्पीड" else "Playback Speed",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${playbackSpeed}x",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showSpeedSelector = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Equalizer,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = if (language == AppLanguage.HINDI) "इक्वालाइज़र / Sound" else "Equalizer & Sound",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = equalizerPreset,
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showEqualizerSelector = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = if (activeSleepTimerMinutes > 0) Color(0xFF00C853) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = if (language == AppLanguage.HINDI) "स्लीप टाइमर" else "Sleep Timer",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = if (activeSleepTimerMinutes > 0) "${activeSleepTimerMinutes} min" else if (language == AppLanguage.HINDI) "बंद" else "Off",
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showSleepTimerSelector = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lyrics,
                                                contentDescription = null,
                                                tint = if (showLyrics) MaterialTheme.colorScheme.primary else Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "बोल (Lyrics)" else "Lyrics Display",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showLyrics = !showLyrics
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "ऑडियो जानकारी" else "Audio Details",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        showInfoModal = true
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Section: Disc or Lyrics
                if (showLyrics) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp)
                            .clickable { showLyrics = false },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B22))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "♪ Synchronized Lyrics ♪",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = playingFile.name.removeSuffix(".mp3"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "🎶 \"High quality audio playback powered by Files by Akash Kumar\" 🎶",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                ),
                                color = Color(0xFF75F9A7),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF2A2E38),
                                            Color(0xFF1C1E24),
                                            Color(0xFF0B0C0E)
                                        )
                                    )
                                )
                                .border(6.dp, Color(0xFF252831), CircleShape)
                                .rotate(if (isPlaying) rotation else 0f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFE91E63),
                                                Color(0xFF9C27B0),
                                                Color(0xFF3F51B5)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF101318)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }

                // Song Title & Waveform
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = playingFile.name.removeSuffix(".mp3").removeSuffix(".wav").removeSuffix(".flac"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = playingFile.artist ?: "High Fidelity Audio • Local",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9AA0A6),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    WaveFormVisualizer(isPlaying = isPlaying, position = positionSeconds)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Slider Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    var isSeeking by remember { mutableStateOf(false) }
                    var seekPercentage by remember { mutableFloatStateOf(0f) }
                    val progressVal = if (isSeeking) seekPercentage else {
                        if (durationSeconds > 0) positionSeconds.toFloat() else 0f
                    }

                    Slider(
                        value = progressVal,
                        onValueChange = {
                            isSeeking = true
                            seekPercentage = it
                        },
                        onValueChangeFinished = {
                            isSeeking = false
                            onSeek(seekPercentage.toInt())
                        },
                        valueRange = 0f..durationSeconds.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color(0xFF2C313C)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(progressVal.toInt()),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9AA0A6)
                        )
                        Text(
                            text = formatDuration(durationSeconds),
                            fontSize = 12.sp,
                            color = Color(0xFF9AA0A6)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    IconButton(
                        onClick = onCycleRepeatMode,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                PlaybackRepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                PlaybackRepeatMode.REPEAT_ALL -> Icons.Default.RepeatOn
                                PlaybackRepeatMode.OFF -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat Mode",
                            tint = if (repeatMode != PlaybackRepeatMode.OFF) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(68.dp))
            }

            if (showSleepTimerSelector) {
                SleepTimerSelectorDialog(
                    language = language,
                    activeMinutes = activeSleepTimerMinutes,
                    onSelect = {
                        onSetSleepTimer(it)
                        showSleepTimerSelector = false
                    },
                    onDismiss = { showSleepTimerSelector = false }
                )
            }

            if (showEqualizerSelector) {
                EqualizerSelectorDialog(
                    language = language,
                    activePreset = equalizerPreset,
                    onSelect = {
                        onSetEqualizer(it)
                        showEqualizerSelector = false
                    },
                    onDismiss = { showEqualizerSelector = false }
                )
            }

            if (showSpeedSelector) {
                PlaybackSpeedSelectorDialog(
                    language = language,
                    currentSpeed = playbackSpeed,
                    onSelect = {
                        onSetSpeed(it)
                        showSpeedSelector = false
                    },
                    onDismiss = { showSpeedSelector = false }
                )
            }

            if (showInfoModal) {
                AudioInfoDialog(
                    language = language,
                    fileItem = playingFile,
                    onDismiss = { showInfoModal = false }
                )
            }
        }
    }
}

@Composable
private fun WaveFormVisualizer(isPlaying: Boolean, position: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(36.dp)
    ) {
        val barCount = 24
        val widthBetweenBars = size.width / barCount
        val barWidth = widthBetweenBars * 0.55f
        for (i in 0 until barCount) {
            val hRatio = if (isPlaying) {
                val waveVal = kotlin.math.sin(phase + i * 0.45).toFloat()
                (0.20f + 0.80f * kotlin.math.abs(waveVal))
            } else 0.15f
            val barHeight = (size.height * hRatio).coerceIn(4f, size.height)
            val x = i * widthBetweenBars + (widthBetweenBars - barWidth) / 2
            val y = (size.height - barHeight) / 2
            drawRoundRect(
                color = if (isPlaying) Color(0xFF00C853) else Color(0xFF42444D),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
private fun EqualizerSelectorDialog(
    language: AppLanguage,
    activePreset: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E212A),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "ऑडियो इक्वालाइज़र" else "Audio Equalizer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (language == AppLanguage.HINDI) "अपनी पसंद की ध्वनि सेटिंग्स चुनें" else "Select audio sound profile preset.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                val presets = listOf("Flat", "Bass Boost", "Vocal Booster", "Rock", "Pop", "Jazz")
                presets.forEach { preset ->
                    val isActive = activePreset.equals(preset, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(preset) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (isActive) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00C853))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun SleepTimerSelectorDialog(
    language: AppLanguage,
    activeMinutes: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E212A),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "स्लीप टाइमर" else "Sleep Timer",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (language == AppLanguage.HINDI)
                        "निर्धारित समय के बाद संगीत स्वतः बंद हो जाएगा।"
                    else
                        "Automatically turn off music playback after specified duration.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                val times = listOf(
                    0 to if (language == AppLanguage.HINDI) "बंद (No Timer)" else "Turn Off Timer",
                    5 to if (language == AppLanguage.HINDI) "5 मिनट" else "5 Minutes",
                    15 to if (language == AppLanguage.HINDI) "15 मिनट" else "15 Minutes",
                    30 to if (language == AppLanguage.HINDI) "30 मिनट" else "30 Minutes",
                    60 to if (language == AppLanguage.HINDI) "1 घंटा (60 mins)" else "1 Hour (60 mins)"
                )
                times.forEach { (minutes, label) ->
                    val isActive = activeMinutes == minutes
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(minutes) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (isActive) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00C853))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

private fun formatDuration(totalSecs: Int): String {
    val m = totalSecs / 60
    val s = totalSecs % 60
    return String.format("%02d:%02d", m, s)
}

@Composable
private fun PlaybackSpeedSelectorDialog(
    language: AppLanguage,
    currentSpeed: Float,
    onSelect: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E212A),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "प्लेबैक गति" else "Playback Speed",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = if (language == AppLanguage.HINDI) "अपनी इच्छानुसार गति चुनें।" else "Choose audio playback speed rate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                val speeds = listOf(
                    0.5f to "0.5x (Slow)",
                    0.75f to "0.75x",
                    1.0f to if (language == AppLanguage.HINDI) "1.0x (सामान्य)" else "1.0x (Normal)",
                    1.25f to "1.25x",
                    1.5f to "1.5x",
                    2.0f to "2.0x (Fast)"
                )
                speeds.forEach { (speed, label) ->
                    val isActive = currentSpeed == speed
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(speed) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (isActive) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00C853))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun AudioInfoDialog(
    language: AppLanguage,
    fileItem: FileItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E212A),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "ऑडियो विवरण" else "Audio Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                InfoRow(label = if (language == AppLanguage.HINDI) "शीर्षक" else "Title", value = fileItem.name)
                InfoRow(label = if (language == AppLanguage.HINDI) "पाथ" else "Path", value = fileItem.path)
                InfoRow(label = if (language == AppLanguage.HINDI) "साइज़" else "Size", value = fileItem.formattedSize)
                InfoRow(label = if (language == AppLanguage.HINDI) "फॉर्मेट" else "Format", value = fileItem.extension.uppercase())

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (language == AppLanguage.HINDI) "बंद करें" else "Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

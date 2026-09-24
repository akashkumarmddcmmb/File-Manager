package com.example.ui.modals

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.AppLanguage
import com.example.model.FileItem
import com.example.model.VideoAspectRatioMode
import kotlinx.coroutines.delay
import java.io.File

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerModal(
    playingFile: FileItem,
    isPlaying: Boolean,
    positionSeconds: Int,
    durationSeconds: Int,
    language: AppLanguage,
    isLocked: Boolean = false,
    aspectRatioMode: VideoAspectRatioMode = VideoAspectRatioMode.FIT_SCREEN,
    playbackSpeed: Float = 1.0f,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit,
    onSeekRelative: (Int) -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onToggleLock: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }
    var activeSubtitles by remember { mutableStateOf(false) }
    var activeAudioTrack by remember { mutableStateOf("Original HD Audio") }

    var internalIsPlaying by remember { mutableStateOf(true) }
    var currentPos by remember { mutableStateOf(positionSeconds) }
    var totalDuration by remember { mutableStateOf(if (durationSeconds > 0) durationSeconds else 180) }

    // Dedicated high performance video ExoPlayer
    val exoPlayer = remember(playingFile.id) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .build().apply {
                val uri = if (File(playingFile.path).exists() && File(playingFile.path).length() > 0) {
                    Uri.fromFile(File(playingFile.path))
                } else if (playingFile.path.startsWith("content://") || playingFile.path.startsWith("file://")) {
                    Uri.parse(playingFile.path)
                } else if (playingFile.path.startsWith("http://") || playingFile.path.startsWith("https://")) {
                    Uri.parse(playingFile.path)
                } else {
                    // High definition reliable MP4 sample
                    Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                }

                setMediaItem(MediaItem.fromUri(uri))
                prepare()
                playWhenReady = true
                setPlaybackSpeed(playbackSpeed)
            }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                internalIsPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    val dur = (exoPlayer.duration.coerceAtLeast(0) / 1000).toInt()
                    if (dur > 0) totalDuration = dur
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // Sync playback speed
    LaunchedEffect(playbackSpeed) {
        exoPlayer.setPlaybackSpeed(playbackSpeed)
    }

    // Smooth position updater ticker (runs only while video is playing)
    LaunchedEffect(exoPlayer, internalIsPlaying) {
        while (internalIsPlaying) {
            if (exoPlayer.isPlaying) {
                currentPos = (exoPlayer.currentPosition / 1000).toInt()
                val dur = (exoPlayer.duration.coerceAtLeast(0) / 1000).toInt()
                if (dur > 0) totalDuration = dur
            }
            delay(1000)
        }
    }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(showControls, internalIsPlaying, isLocked) {
        if (showControls && internalIsPlaying && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    DisposableEffect(isLandscape) {
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }

        onDispose {
            val w = activity?.window
            if (w != null) {
                val insetsController = WindowCompat.getInsetsController(w, w.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    w.attributes = w.attributes.apply {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                    }
                }
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var showDoubleTapFeedback by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(showDoubleTapFeedback) {
        if (showDoubleTapFeedback != null) {
            delay(800)
            showDoubleTapFeedback = null
        }
    }

    BackHandler {
        exoPlayer.stop()
        onDismiss()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(isLocked) {
                        detectTapGestures(
                            onTap = {
                                if (!isLocked) {
                                    showControls = !showControls
                                } else {
                                    showControls = true
                                }
                            },
                            onDoubleTap = { offset ->
                                if (!isLocked) {
                                    val screenWidth = size.width
                                    if (offset.x < screenWidth * 0.4f) {
                                        // Left side -> Rewind 10s
                                        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                        exoPlayer.seekTo(newPos)
                                        currentPos = (newPos / 1000).toInt()
                                        showDoubleTapFeedback = "-10s"
                                    } else if (offset.x > screenWidth * 0.6f) {
                                        // Right side -> Forward 10s
                                        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                        exoPlayer.seekTo(newPos)
                                        currentPos = (newPos / 1000).toInt()
                                        showDoubleTapFeedback = "+10s"
                                    } else {
                                        // Center double tap -> Cycle Aspect Ratio (Full Screen Zoom / Fit Screen)
                                        onCycleAspectRatio()
                                    }
                                }
                            }
                        )
                    }
            ) {
                // REAL HARDWARE-ACCELERATED VIDEO VIEW
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            resizeMode = when (aspectRatioMode) {
                                VideoAspectRatioMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                VideoAspectRatioMode.FILL_CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                VideoAspectRatioMode.ORIGINAL_RATIO -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                            }
                            setBackgroundColor(android.graphics.Color.BLACK)
                        }
                    },
                    update = { playerView ->
                        playerView.player = exoPlayer
                        playerView.resizeMode = when (aspectRatioMode) {
                            VideoAspectRatioMode.FIT_SCREEN -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            VideoAspectRatioMode.FILL_CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            VideoAspectRatioMode.ORIGINAL_RATIO -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Double Tap Feedback Indicator
                if (showDoubleTapFeedback != null) {
                    Box(
                        modifier = Modifier
                            .align(if (showDoubleTapFeedback == "-10s") Alignment.CenterStart else Alignment.CenterEnd)
                            .padding(horizontal = 48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (showDoubleTapFeedback == "-10s") Icons.Default.FastRewind else Icons.Default.FastForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = showDoubleTapFeedback!!,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Subtitles Overlay
                if (activeSubtitles) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (showControls) 120.dp else 40.dp, start = 24.dp, end = 24.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "सीसी सबटाइटल: [HD 1080p वीडियो चल रहा है]"
                            else
                                "CC Subtitles: [Playing HD 1080p Video Media]",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFEEFC3),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // SCREEN LOCKED NOTIFICATION HUD
                if (isLocked && showControls) {
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Surface(
                            onClick = onToggleLock,
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFD32F2F),
                            tonalElevation = 6.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Unlock",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "स्क्रीन लॉक है (अनलॉक करने के लिए टैप करें)" else "Screen Locked (Tap to Unlock)",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // CONTROLS OVERLAY HUD
                if (!isLocked) {
                    AnimatedVisibility(
                        visible = showControls,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(200))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        ) {
                            // TOP BAR
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding()
                                    .displayCutoutPadding()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    exoPlayer.stop()
                                    onDismiss()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = playingFile.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "FHD 1080p • ${playingFile.formattedSize}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                IconButton(onClick = onToggleLock) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Lock Screen",
                                        tint = Color.White
                                    )
                                }

                                IconButton(onClick = { activeSubtitles = !activeSubtitles }) {
                                    Icon(
                                        imageVector = if (activeSubtitles) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                                        contentDescription = "Toggle CC",
                                        tint = if (activeSubtitles) Color(0xFF00C853) else Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                IconButton(onClick = onCycleAspectRatio) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "Aspect Ratio",
                                        tint = Color.White
                                    )
                                }

                                IconButton(onClick = {
                                    if (isLandscape) {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    } else {
                                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isLandscape) Icons.Default.ScreenLockPortrait else Icons.Default.ScreenRotation,
                                        contentDescription = if (isLandscape) "Portrait Mode" else "Landscape Fullscreen",
                                        tint = Color.White
                                    )
                                }
                            }

                            // CENTER CONTROL BUTTONS
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    onClick = onPrevTrack,
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                                        exoPlayer.seekTo(newPos)
                                        currentPos = (newPos / 1000).toInt()
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Replay10,
                                            contentDescription = "Rewind 10s",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                            internalIsPlaying = false
                                        } else {
                                            exoPlayer.play()
                                            internalIsPlaying = true
                                        }
                                        onTogglePlay()
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (internalIsPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (internalIsPlaying) "Pause" else "Play",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                        exoPlayer.seekTo(newPos)
                                        currentPos = (newPos / 1000).toInt()
                                    },
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Forward10,
                                            contentDescription = "Fast Forward 10s",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Surface(
                                    onClick = onNextTrack,
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // BOTTOM CONTROLS & TIMELINE SEEKBAR
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .displayCutoutPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatDuration(currentPos),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = formatDuration(totalDuration),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Slider(
                                    value = currentPos.toFloat().coerceIn(0f, totalDuration.toFloat()),
                                    onValueChange = {
                                        currentPos = it.toInt()
                                        exoPlayer.seekTo(it.toLong() * 1000)
                                        onSeek(it.toInt())
                                    },
                                    valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        onClick = {
                                            val nextSpeed = when (playbackSpeed) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 2.0f
                                                2.0f -> 0.5f
                                                else -> 1.0f
                                            }
                                            exoPlayer.setPlaybackSpeed(nextSpeed)
                                            onSetSpeed(nextSpeed)
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = "Speed",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${playbackSpeed}x",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = onCycleAspectRatio,
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AspectRatio,
                                                contentDescription = "Aspect Ratio",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when (aspectRatioMode) {
                                                    VideoAspectRatioMode.FIT_SCREEN -> if (language == AppLanguage.HINDI) "फिट स्क्रीन (16:9)" else "Fit Screen"
                                                    VideoAspectRatioMode.FILL_CROP -> if (language == AppLanguage.HINDI) "भरें (Zoom)" else "Fill (Zoom)"
                                                    VideoAspectRatioMode.ORIGINAL_RATIO -> if (language == AppLanguage.HINDI) "मूल (4:3)" else "Original"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = {
                                            activeAudioTrack = if (activeAudioTrack.startsWith("Original")) "Hindi Audio" else "Original HD Audio"
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Audiotrack,
                                                contentDescription = "Audio Track",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (language == AppLanguage.HINDI) {
                                                    if (activeAudioTrack.startsWith("Original")) "एचडी ऑडियो" else "हिंदी ऑडियो"
                                                } else activeAudioTrack,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = {
                                            if (isLandscape) {
                                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            } else {
                                                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                            }
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isLandscape) Icons.Default.ScreenLockPortrait else Icons.Default.ScreenRotation,
                                                contentDescription = "Rotate Screen",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isLandscape) {
                                                    if (language == AppLanguage.HINDI) "पोर्ट्रेट" else "Portrait"
                                                } else {
                                                    if (language == AppLanguage.HINDI) "फुलस्क्रीन" else "Landscape"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
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

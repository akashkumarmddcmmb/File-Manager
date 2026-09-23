package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

class RealAudioEngine(private var defaultContext: Context? = null) {
    private val tag = "RealAudioEngine"
    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingState = false
    private var currentDurationSeconds = 240
    private var currentPositionSeconds = 0
    
    private val handler = Handler(Looper.getMainLooper())
    private var progressListener: ((positionSeconds: Int, durationSeconds: Int, isPlaying: Boolean) -> Unit)? = null
    private var completionListener: (() -> Unit)? = null

    private val progressRunnable = object : Runnable {
        override fun run() {
            try {
                mediaPlayer?.let { player ->
                    if (isPlayingState && player.isPlaying) {
                        val currentMs = player.currentPosition
                        val durMs = player.duration
                        val posSec = (currentMs / 1000).coerceAtLeast(0)
                        val durSec = if (durMs > 0) (durMs / 1000) else currentDurationSeconds
                        currentPositionSeconds = posSec
                        progressListener?.invoke(posSec, durSec, true)
                        handler.postDelayed(this, 500)
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Progress tracker exception: ${e.message}")
            }
            if (isPlayingState) {
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun setContext(context: Context) {
        this.defaultContext = context
    }

    fun setProgressListener(listener: (positionSeconds: Int, durationSeconds: Int, isPlaying: Boolean) -> Unit) {
        this.progressListener = listener
    }

    fun setCompletionListener(listener: () -> Unit) {
        this.completionListener = listener
    }

    fun startPlaying(
        fileName: String,
        filePath: String,
        startSeconds: Int = 0,
        ctx: Context? = null
    ): Int {
        stop()

        val context = ctx ?: defaultContext
        Log.d(tag, "startPlaying called for: $fileName, path: $filePath")

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                
                // Resolve data source
                var sourceSet = false
                if (filePath.startsWith("content://") && context != null) {
                    try {
                        setDataSource(context, Uri.parse(filePath))
                        sourceSet = true
                    } catch (e: Exception) {
                        Log.w(tag, "Content URI failed: ${e.message}")
                    }
                }
                
                if (!sourceSet) {
                    val localFile = File(filePath)
                    if (localFile.exists() && localFile.length() > 0) {
                        try {
                            setDataSource(localFile.absolutePath)
                            sourceSet = true
                        } catch (e: Exception) {
                            Log.w(tag, "Local file dataSource failed: ${e.message}")
                        }
                    }
                }

                // If demo file or network/storage missing, use synthesized melody
                if (!sourceSet && context != null) {
                    try {
                        val sampleFile = AudioSampleGenerator.getOrCreateSampleAudio(context, fileName, filePath)
                        setDataSource(sampleFile.absolutePath)
                        sourceSet = true
                    } catch (e: Exception) {
                        Log.w(tag, "Sample WAV dataSource failed: ${e.message}")
                    }
                }

                if (!sourceSet) {
                    // Fallback to sample URL
                    setDataSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
                }

                isLooping = false

                setOnPreparedListener { player ->
                    try {
                        val durMs = player.duration
                        if (durMs > 0) {
                            currentDurationSeconds = durMs / 1000
                        }
                        if (startSeconds > 0 && startSeconds < currentDurationSeconds) {
                            player.seekTo(startSeconds * 1000)
                        }
                        player.start()
                        isPlayingState = true
                        progressListener?.invoke(startSeconds, currentDurationSeconds, true)
                        startProgressTracker()
                    } catch (e: Exception) {
                        Log.e(tag, "OnPrepared start failed: ${e.message}", e)
                    }
                }

                setOnCompletionListener {
                    isPlayingState = false
                    stopProgressTracker()
                    progressListener?.invoke(currentDurationSeconds, currentDurationSeconds, false)
                    completionListener?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(tag, "MediaPlayer error: what=$what extra=$extra")
                    isPlayingState = false
                    stopProgressTracker()
                    true // Handled
                }

                prepareAsync()
            }

            mediaPlayer = mp
            return currentDurationSeconds
        } catch (e: Exception) {
            Log.e(tag, "Failed to start audio playback: ${e.message}", e)
            isPlayingState = false
            return 240
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                }
                isPlayingState = true
                progressListener?.invoke(currentPositionSeconds, currentDurationSeconds, true)
                startProgressTracker()
            }
        } catch (e: Exception) {
            Log.e(tag, "Resume failed: ${e.message}")
        }
    }

    fun pause() {
        try {
            isPlayingState = false
            stopProgressTracker()
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
            progressListener?.invoke(currentPositionSeconds, currentDurationSeconds, false)
        } catch (e: Exception) {
            Log.e(tag, "Pause failed: ${e.message}")
        }
    }

    fun stop() {
        try {
            isPlayingState = false
            stopProgressTracker()
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (_: Exception) {}
                try {
                    player.reset()
                } catch (_: Exception) {}
                try {
                    player.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.e(tag, "Stop failed: ${e.message}")
        } finally {
            mediaPlayer = null
            currentPositionSeconds = 0
            progressListener?.invoke(0, currentDurationSeconds, false)
        }
    }

    fun seekTo(seconds: Int) {
        try {
            currentPositionSeconds = seconds
            mediaPlayer?.seekTo(seconds * 1000)
            progressListener?.invoke(seconds, currentDurationSeconds, isPlayingState)
        } catch (e: Exception) {
            Log.e(tag, "Seek failed: ${e.message}")
        }
    }

    fun getCurrentPositionSeconds(): Int {
        return try {
            val ms = mediaPlayer?.currentPosition ?: (currentPositionSeconds * 1000)
            (ms / 1000).coerceAtLeast(0)
        } catch (e: Exception) {
            currentPositionSeconds
        }
    }

    fun isPlaying(): Boolean {
        return try {
            isPlayingState && (mediaPlayer?.isPlaying == true)
        } catch (e: Exception) {
            isPlayingState
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaPlayer?.let { mp ->
                    mp.playbackParams = mp.playbackParams.setSpeed(speed.coerceIn(0.25f, 2.5f))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Speed update failed: ${e.message}")
        }
    }

    fun setEqualizerPreset(preset: String) {
        Log.d(tag, "Equalizer preset set to $preset")
    }

    fun setVolume(vol: Float) {
        try {
            val clamped = vol.coerceIn(0f, 1f)
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (e: Exception) {
            Log.e(tag, "Volume update failed: ${e.message}")
        }
    }

    private fun startProgressTracker() {
        handler.removeCallbacks(progressRunnable)
        handler.post(progressRunnable)
    }

    private fun stopProgressTracker() {
        handler.removeCallbacks(progressRunnable)
    }
}

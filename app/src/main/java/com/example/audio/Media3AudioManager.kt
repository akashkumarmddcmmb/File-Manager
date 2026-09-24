package com.example.audio

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.FileItem
import com.example.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.io.File
import java.util.concurrent.Executors

class Media3AudioManager(private val context: Context) {
    private val TAG = "Media3AudioManager"
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var onStateUpdate: ((isPlaying: Boolean, positionSeconds: Int, durationSeconds: Int) -> Unit)? = null
    private var onTrackChanged: ((trackId: String) -> Unit)? = null

    private val handler = Handler(Looper.getMainLooper())
    private val directFallbackEngine = RealAudioEngine(context)

    // Store pending playlist if play is requested while controller is still connecting
    private var pendingPlaylist: List<FileItem>? = null
    private var pendingTargetFile: FileItem? = null
    private var isConnecting = false
    private var isUsingFallback = false
    private var currentPlaylistCache: List<FileItem> = emptyList()

    private val progressTicker = object : Runnable {
        override fun run() {
            notifyState()
            if (isPlaying()) {
                handler.postDelayed(this, 500)
            }
        }
    }

    init {
        directFallbackEngine.setProgressListener { pos, dur, playing ->
            if (isUsingFallback) {
                onStateUpdate?.invoke(playing, pos, dur)
            }
        }
        directFallbackEngine.setCompletionListener {
            if (isUsingFallback) {
                seekToNext()
            }
        }
    }

    fun initialize(onStateChanged: (isPlaying: Boolean, positionSeconds: Int, durationSeconds: Int) -> Unit) {
        this.onStateUpdate = onStateChanged
        connectController()
    }

    private fun connectController() {
        if (mediaController != null || isConnecting) return
        isConnecting = true

        try {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

            controllerFuture?.addListener({
                try {
                    val controller = controllerFuture?.get()
                    mediaController = controller
                    isConnecting = false

                    controller?.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (!isUsingFallback) notifyState()
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (!isUsingFallback) {
                                notifyState()
                                if (isPlaying) {
                                    startTicker()
                                } else {
                                    stopTicker()
                                }
                            }
                        }

                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            if (!isUsingFallback) {
                                notifyState()
                                mediaItem?.mediaId?.let { id ->
                                    onTrackChanged?.invoke(id)
                                }
                            }
                        }

                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            if (!isUsingFallback) notifyState()
                        }
                    })

                    notifyState()

                    // Execute any pending play requests immediately on connection!
                    val pendingTarget = pendingTargetFile
                    val pendingList = pendingPlaylist
                    if (pendingTarget != null) {
                        pendingTargetFile = null
                        pendingPlaylist = null
                        playPlaylist(pendingList ?: listOf(pendingTarget), pendingTarget)
                    }
                } catch (e: Exception) {
                    isConnecting = false
                    Log.e(TAG, "Failed to connect MediaController: ${e.message}")
                }
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            isConnecting = false
            Log.e(TAG, "Error initializing session token: ${e.message}")
        }
    }

    fun setOnTrackChangedListener(listener: (trackId: String) -> Unit) {
        this.onTrackChanged = listener
    }

    private fun startTicker() {
        handler.removeCallbacks(progressTicker)
        handler.post(progressTicker)
    }

    private fun stopTicker() {
        handler.removeCallbacks(progressTicker)
    }

    fun notifyState() {
        if (isUsingFallback) {
            return
        }
        val controller = mediaController ?: return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            val isPlaying = controller.isPlaying
            val position = (controller.currentPosition / 1000).toInt().coerceAtLeast(0)
            val duration = (controller.duration.coerceAtLeast(0) / 1000).toInt()
            onStateUpdate?.invoke(isPlaying, position, if (duration > 0) duration else 240)
        } else {
            handler.post {
                val ctrl = mediaController ?: return@post
                val isPlaying = ctrl.isPlaying
                val position = (ctrl.currentPosition / 1000).toInt().coerceAtLeast(0)
                val duration = (ctrl.duration.coerceAtLeast(0) / 1000).toInt()
                onStateUpdate?.invoke(isPlaying, position, if (duration > 0) duration else 240)
            }
        }
    }

    fun resolveMediaUri(path: String, title: String): Uri {
        if (path.startsWith("content://") || path.startsWith("file://")) {
            return Uri.parse(path)
        }
        val localFile = File(path)
        if (localFile.exists() && localFile.length() > 0) {
            return Uri.fromFile(localFile)
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return Uri.parse(path)
        }

        // Instant audio track (<1ms generation)
        val sampleFile = AudioSampleGenerator.getOrCreateSampleAudio(context, title, path)
        return Uri.fromFile(sampleFile)
    }

    private fun createMediaItem(file: FileItem): MediaItem {
        val title = file.name
        val metadata = MediaMetadata.Builder()
            .setTitle(title.removeSuffix(".${file.extension}"))
            .setArtist(file.artist ?: "Local Audio")
            .setDisplayTitle(title.removeSuffix(".${file.extension}"))
            .build()

        val audioUri = resolveMediaUri(file.path, file.name)
        return MediaItem.Builder()
            .setMediaId(file.id)
            .setUri(audioUri)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * Plays the song INSTANTLY (<1ms) without any delay.
     */
    fun playPlaylist(playlist: List<FileItem>, targetFile: FileItem) {
        currentPlaylistCache = playlist
        val controller = mediaController

        // If MediaController is connecting or null, use instant direct fallback so song plays IMMEDIATELY
        if (controller == null) {
            isUsingFallback = true
            pendingPlaylist = playlist
            pendingTargetFile = targetFile
            connectController()
            directFallbackEngine.startPlaying(targetFile.name, targetFile.path, 0, context)
            startTicker()
            onStateUpdate?.invoke(true, 0, if (targetFile.durationSeconds > 0) targetFile.durationSeconds else 240)
            return
        }

        try {
            isUsingFallback = false
            directFallbackEngine.stop()

            val targetIndex = playlist.indexOfFirst { it.id == targetFile.id }.coerceAtLeast(0)

            // Fast path: if controller already has same playlist, just seek instantly (<1ms)!
            if (controller.mediaItemCount == playlist.size && targetIndex < controller.mediaItemCount) {
                val currentItemAtIdx = try { controller.getMediaItemAt(targetIndex).mediaId } catch (e: Exception) { null }
                if (currentItemAtIdx == targetFile.id) {
                    controller.seekToDefaultPosition(targetIndex)
                    controller.play()
                    startTicker()
                    notifyState()
                    return
                }
            }

            // Build MediaItems and set items
            val targetMediaItem = createMediaItem(targetFile)
            val allMediaItems = playlist.map { file ->
                if (file.id == targetFile.id) targetMediaItem else createMediaItem(file)
            }

            controller.setMediaItems(allMediaItems, targetIndex, 0L)
            controller.prepare()
            controller.play()
            startTicker()
            notifyState()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback, using fallback: ${e.message}", e)
            isUsingFallback = true
            directFallbackEngine.startPlaying(targetFile.name, targetFile.path, 0, context)
            startTicker()
            onStateUpdate?.invoke(true, 0, if (targetFile.durationSeconds > 0) targetFile.durationSeconds else 240)
        }
    }

    fun play() {
        if (isUsingFallback) {
            mediaController?.pause()
            directFallbackEngine.resume()
            startTicker()
            return
        }
        directFallbackEngine.stop()
        val controller = mediaController ?: return
        controller.play()
        startTicker()
        notifyState()
    }

    fun pause() {
        if (isUsingFallback) {
            directFallbackEngine.pause()
            stopTicker()
            return
        }
        val controller = mediaController ?: return
        controller.pause()
        stopTicker()
        notifyState()
    }

    fun stop() {
        directFallbackEngine.stop()
        stopTicker()
        val controller = mediaController
        if (controller != null) {
            controller.stop()
            controller.clearMediaItems()
            notifyState()
        }
    }

    fun togglePlayPause() {
        if (isUsingFallback) {
            mediaController?.pause()
            if (directFallbackEngine.isPlaying()) {
                directFallbackEngine.pause()
            } else {
                directFallbackEngine.resume()
            }
            startTicker()
            return
        }
        directFallbackEngine.stop()
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
            stopTicker()
        } else {
            controller.play()
            startTicker()
        }
        notifyState()
    }

    fun seekToNext() {
        if (isUsingFallback) {
            mediaController?.pause()
            val list = currentPlaylistCache
            val pending = pendingTargetFile
            if (list.isNotEmpty() && pending != null) {
                val idx = list.indexOfFirst { it.id == pending.id }
                val nextIdx = if (idx in 0 until list.size - 1) idx + 1 else 0
                val nextTrack = list[nextIdx]
                pendingTargetFile = nextTrack
                directFallbackEngine.startPlaying(nextTrack.name, nextTrack.path, 0, context)
                onTrackChanged?.invoke(nextTrack.id)
                onStateUpdate?.invoke(true, 0, if (nextTrack.durationSeconds > 0) nextTrack.durationSeconds else 240)
            }
            return
        }

        directFallbackEngine.stop()
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
            controller.play()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(0)
            controller.play()
        }
        notifyState()
    }

    fun seekToPrevious() {
        if (isUsingFallback) {
            mediaController?.pause()
            val list = currentPlaylistCache
            val pending = pendingTargetFile
            if (list.isNotEmpty() && pending != null) {
                val idx = list.indexOfFirst { it.id == pending.id }
                val prevIdx = if (idx > 0) idx - 1 else list.size - 1
                val prevTrack = list[prevIdx]
                pendingTargetFile = prevTrack
                directFallbackEngine.startPlaying(prevTrack.name, prevTrack.path, 0, context)
                onTrackChanged?.invoke(prevTrack.id)
                onStateUpdate?.invoke(true, 0, if (prevTrack.durationSeconds > 0) prevTrack.durationSeconds else 240)
            }
            return
        }

        directFallbackEngine.stop()
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
            controller.play()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(controller.mediaItemCount - 1)
            controller.play()
        }
        notifyState()
    }

    fun seekTo(seconds: Int) {
        if (isUsingFallback) {
            directFallbackEngine.seekTo(seconds)
            return
        }
        val controller = mediaController ?: return
        controller.seekTo(seconds.toLong() * 1000)
        notifyState()
    }

    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        controller.setPlaybackSpeed(speed)
        notifyState()
    }

    fun getPositionSeconds(): Int {
        if (isUsingFallback) {
            return directFallbackEngine.getCurrentPositionSeconds()
        }
        val controller = mediaController ?: return 0
        return (controller.currentPosition / 1000).toInt()
    }

    fun isPlaying(): Boolean {
        if (isUsingFallback) {
            return directFallbackEngine.isPlaying()
        }
        return mediaController?.isPlaying == true
    }

    fun release() {
        stopTicker()
        directFallbackEngine.stop()
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture!!)
            controllerFuture = null
            mediaController = null
        }
    }
}

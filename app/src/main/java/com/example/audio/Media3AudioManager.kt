package com.example.audio

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class Media3AudioManager(private val context: Context) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var onStateUpdate: ((isPlaying: Boolean, positionSeconds: Int, durationSeconds: Int) -> Unit)? = null
    private var onTrackChanged: ((trackId: String) -> Unit)? = null

    fun initialize(onStateChanged: (isPlaying: Boolean, positionSeconds: Int, durationSeconds: Int) -> Unit) {
        this.onStateUpdate = onStateChanged
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        notifyState()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        notifyState()
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        notifyState()
                        mediaItem?.mediaId?.let { id ->
                            onTrackChanged?.invoke(id)
                        }
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        notifyState()
                    }
                })
                notifyState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    fun setOnTrackChangedListener(listener: (trackId: String) -> Unit) {
        this.onTrackChanged = listener
    }

    private fun notifyState() {
        val controller = mediaController ?: return
        val isPlaying = controller.isPlaying
        val position = (controller.currentPosition / 1000).toInt()
        val duration = (controller.duration.coerceAtLeast(0) / 1000).toInt()
        onStateUpdate?.invoke(isPlaying, position, if (duration > 0) duration else 268)
    }

    private fun resolveMediaUri(path: String, title: String): Uri {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return Uri.parse(path)
        }
        if (path.startsWith("content://") || path.startsWith("file://")) {
            return Uri.parse(path)
        }
        val localFile = java.io.File(path)
        if (localFile.exists() && localFile.length() > 0) {
            return Uri.fromFile(localFile)
        }
        // Fallback for mock demo items when physical files don't exist on device storage
        val fallbackUrl = when {
            title.contains("Kesariya", ignoreCase = true) -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            title.contains("Chaleya", ignoreCase = true) -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            title.contains("Tum Hi Ho", ignoreCase = true) -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            title.contains("Guitar", ignoreCase = true) || title.contains("Acoustic", ignoreCase = true) -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            title.contains("Lofi", ignoreCase = true) || title.contains("Ambient", ignoreCase = true) -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
            title.contains("Voice Note", ignoreCase = true) || title.contains("Akash", ignoreCase = true) -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
            else -> "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
        }
        return Uri.parse(fallbackUrl)
    }

    fun playPlaylist(playlist: List<com.example.model.FileItem>, targetFile: com.example.model.FileItem) {
        val controller = mediaController ?: return
        if (playlist.isEmpty()) return

        val mediaItems = playlist.map { file ->
            val title = file.name
            val artworkUrl = when {
                title.contains("Kesariya", ignoreCase = true) -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=512&auto=format&fit=crop"
                title.contains("Chaleya", ignoreCase = true) -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=512&auto=format&fit=crop"
                title.contains("Tum Hi Ho", ignoreCase = true) -> "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=512&auto=format&fit=crop"
                title.contains("Guitar", ignoreCase = true) || title.contains("Acoustic", ignoreCase = true) -> "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=512&auto=format&fit=crop"
                else -> "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=512&auto=format&fit=crop"
            }
            val metadata = MediaMetadata.Builder()
                .setTitle(title.removeSuffix(".${file.extension}"))
                .setArtist(file.artist ?: "Local Music - Akash Kumar")
                .setDisplayTitle(title.removeSuffix(".${file.extension}"))
                .setArtworkUri(Uri.parse(artworkUrl))
                .build()

            val audioUri = resolveMediaUri(file.path, file.name)
            MediaItem.Builder()
                .setMediaId(file.id)
                .setUri(audioUri)
                .setMediaMetadata(metadata)
                .build()
        }

        val startIndex = playlist.indexOfFirst { it.id == targetFile.id }.coerceAtLeast(0)
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        notifyState()
    }

    fun playTrack(title: String, artist: String, uriString: String) {
        val controller = mediaController ?: return
        
        val artworkUrl = when {
            title.contains("Kesariya", ignoreCase = true) -> "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=512&auto=format&fit=crop"
            title.contains("Chaleya", ignoreCase = true) -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=512&auto=format&fit=crop"
            title.contains("Tum Hi Ho", ignoreCase = true) -> "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=512&auto=format&fit=crop"
            title.contains("Guitar", ignoreCase = true) || title.contains("Acoustic", ignoreCase = true) -> "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=512&auto=format&fit=crop"
            else -> "https://images.unsplash.com/photo-1507838153414-b4b713384a76?w=512&auto=format&fit=crop"
        }
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setDisplayTitle(title)
            .setArtworkUri(Uri.parse(artworkUrl))
            .build()

        val audioUri = resolveMediaUri(uriString, title)
        val mediaItem = MediaItem.Builder()
            .setMediaId(title)
            .setUri(audioUri)
            .setMediaMetadata(metadata)
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
        notifyState()
    }

    fun seekToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(0)
        }
        notifyState()
    }

    fun seekToPrevious() {
        val controller = mediaController ?: return
        if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else if (controller.mediaItemCount > 0) {
            controller.seekToDefaultPosition(controller.mediaItemCount - 1)
        }
        notifyState()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
        notifyState()
    }

    fun seekTo(seconds: Int) {
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
        val controller = mediaController ?: return 0
        return (controller.currentPosition / 1000).toInt()
    }

    fun release() {
        if (controllerFuture != null) {
            MediaController.releaseFuture(controllerFuture!!)
            controllerFuture = null
            mediaController = null
        }
    }
}

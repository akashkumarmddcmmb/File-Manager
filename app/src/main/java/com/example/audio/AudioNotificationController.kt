package com.example.audio

interface AudioNotificationListener {
    fun onPlayPause()
    fun onPrevious()
    fun onNext()
    fun onStop()
    fun onSeekTo(seconds: Int)
}

object AudioNotificationController {
    var listener: AudioNotificationListener? = null

    fun triggerPlayPause() {
        listener?.onPlayPause()
    }

    fun triggerPrevious() {
        listener?.onPrevious()
    }

    fun triggerNext() {
        listener?.onNext()
    }

    fun triggerStop() {
        listener?.onStop()
    }

    fun triggerSeekTo(seconds: Int) {
        listener?.onSeekTo(seconds)
    }
}

package com.example.audio

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

object AudioSampleGenerator {
    private const val TAG = "AudioSampleGenerator"
    private const val SAMPLE_RATE = 44100
    private const val DURATION_SECONDS = 30

    fun getOrCreateSampleAudio(context: Context, title: String, path: String): File {
        val cacheDir = File(context.cacheDir, "audio_samples")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val styleIndex = Math.floorMod(title.hashCode() + path.hashCode(), 6)
        val sampleFile = File(cacheDir, "track_melody_$styleIndex.wav")

        if (sampleFile.exists() && sampleFile.length() > 44) {
            return sampleFile
        }

        try {
            generateCleanWavFile(sampleFile, styleIndex)
            Log.d(TAG, "Generated clean demo music track style #$styleIndex at ${sampleFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating sample audio: ${e.message}", e)
        }

        return sampleFile
    }

    private fun generateCleanWavFile(outputFile: File, style: Int) {
        val totalSamples = SAMPLE_RATE * DURATION_SECONDS
        val pcmData = ByteArray(totalSamples * 2) // 16-bit mono

        // Clean, pleasant single melody notes per style
        val notes = when (style) {
            0 -> doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25) // Raag Bhupali Pentatonic
            1 -> doubleArrayOf(329.63, 392.00, 440.00, 493.88, 587.33, 659.25) // Upbeat Melodic
            2 -> doubleArrayOf(220.00, 261.63, 293.66, 329.63, 349.23, 440.00) // Romantic Minor
            3 -> doubleArrayOf(196.00, 246.94, 293.66, 392.00, 440.00, 493.88) // Acoustic G Major
            4 -> doubleArrayOf(174.61, 220.00, 261.63, 329.63, 392.00, 440.00) // Soft Lo-Fi
            else -> doubleArrayOf(261.63, 329.63, 392.00, 523.25, 659.25, 783.99) // Pop Melodic
        }

        val noteDuration = (SAMPLE_RATE * 0.50).toInt() // 500ms per note
        var currentNoteIndex = 0
        var phase = 0.0

        for (i in 0 until totalSamples) {
            if (i % noteDuration == 0) {
                currentNoteIndex = (currentNoteIndex + 1) % notes.size
            }

            val freq = notes[currentNoteIndex]
            val timeInNote = (i % noteDuration).toDouble() / SAMPLE_RATE
            // Smooth piano-like decay envelope
            val envelope = kotlin.math.exp(-timeInNote * 2.2)

            // Crystal-clear single pure tone with gentle second harmonic
            val sampleVal = (sin(phase) * 0.75 + sin(phase * 2.0) * 0.25) * envelope * 0.7

            phase += 2.0 * PI * freq / SAMPLE_RATE
            if (phase > 2.0 * PI) phase -= 2.0 * PI

            val sampleShort = (sampleVal.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
            val byteIndex = i * 2
            pcmData[byteIndex] = (sampleShort.toInt() and 0xFF).toByte()
            pcmData[byteIndex + 1] = ((sampleShort.toInt() shr 8) and 0xFF).toByte()
        }

        FileOutputStream(outputFile).use { fos ->
            writeWavHeader(fos, totalSamples * 2, SAMPLE_RATE, 1, 16)
            fos.write(pcmData)
            fos.flush()
        }
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        audioDataLength: Int,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val totalDataLen = audioDataLength + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalDataLen)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = PCM
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(audioDataLength)

        out.write(header)
    }
}

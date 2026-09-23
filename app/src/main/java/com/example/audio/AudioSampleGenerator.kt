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
    private const val DURATION_SECONDS = 30 // 30-second rich melody loop

    /**
     * Returns a local File containing high-quality synthesized music for the given track name/path.
     * Caches the file in the app cache directory so generation happens only once.
     */
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
            generateWavFile(sampleFile, styleIndex)
            Log.d(TAG, "Generated demo music track style #$styleIndex at ${sampleFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating sample audio: ${e.message}", e)
        }

        return sampleFile
    }

    private fun generateWavFile(outputFile: File, style: Int) {
        val totalSamples = SAMPLE_RATE * DURATION_SECONDS
        val pcmData = ByteArray(totalSamples * 2) // 16-bit mono

        // Different musical melodies per style
        val notes = when (style) {
            0 -> doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25) // Kesariya / Indian Pentatonic (C, D, E, G, A, C)
            1 -> doubleArrayOf(329.63, 392.00, 440.00, 493.88, 587.33, 659.25) // Chaleya / Upbeat Dance (E, G, A, B, D, E)
            2 -> doubleArrayOf(220.00, 261.63, 293.66, 329.63, 349.23, 440.00) // Tum Hi Ho / Romantic A-Minor (A, C, D, E, F, A)
            3 -> doubleArrayOf(196.00, 246.94, 293.66, 392.00, 440.00, 493.88) // Acoustic Guitar G-Major Pluck (G, B, D, G, A, B)
            4 -> doubleArrayOf(174.61, 220.00, 261.63, 329.63, 392.00, 440.00) // Lo-Fi Chill F-Maj7 (F, A, C, E, G, A)
            else -> doubleArrayOf(261.63, 329.63, 392.00, 523.25, 659.25, 783.99) // Pop / EDM Melody
        }

        val noteDuration = (SAMPLE_RATE * 0.45).toInt() // ~450ms per note
        var currentNoteIndex = 0
        var phase = 0.0

        for (i in 0 until totalSamples) {
            if (i % noteDuration == 0) {
                currentNoteIndex = (currentNoteIndex + 1) % notes.size
            }

            val freq = notes[currentNoteIndex]
            val bassFreq = freq / 2.0 // Warm bass octave underneath

            val timeInNote = (i % noteDuration).toDouble() / SAMPLE_RATE
            // Attack-Decay-Sustain envelope to sound like piano/guitar/instrument
            val envelope = kotlin.math.exp(-timeInNote * 2.8)

            // Polyphonic harmonics (Fundamental + 2nd Harmonic + Bass + Vibrato)
            val vibrato = 1.0 + 0.008 * sin(2.0 * PI * 5.0 * i / SAMPLE_RATE)
            val sampleVal = (
                sin(phase * vibrato) * 0.6 +
                sin(phase * 2.0 * vibrato) * 0.25 +
                sin(2.0 * PI * bassFreq * i / SAMPLE_RATE) * 0.35
            ) * envelope * 0.75

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

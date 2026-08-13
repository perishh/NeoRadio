package com.example.neoradio.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import com.example.neoradio.repository.WaveformRepository
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

@OptIn(UnstableApi::class)
class WaveformAudioProcessor(
    private val barCount: Int = 32
) : BaseAudioProcessor() {

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        // Ensure format is PCM 16-bit
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val position = inputBuffer.position()
        val limit = inputBuffer.limit()
        val remaining = limit - position

        if (remaining <= 0) return

        // Duplicate buffer to process amplitude without disturbing output stream
        val shortBuffer = inputBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val totalSamples = shortBuffer.remaining()

        if (totalSamples > 0) {
            val bars = FloatArray(barCount)
            val samplesPerBar = (totalSamples / barCount).coerceAtLeast(1)

            for (i in 0 until barCount) {
                var sum = 0.0
                val start = i * samplesPerBar
                val end = (start + samplesPerBar).coerceAtMost(totalSamples)
                var count = 0

                for (j in start until end) {
                    val sample = shortBuffer.get(j) / 32768.0f // Normalize -1.0 to 1.0
                    sum += sample * sample
                    count++
                }

                val rms = if (count > 0) sqrt(sum / count).toFloat() else 0f
                bars[i] = (rms * 2.5f).coerceIn(0.05f, 1f) // Scale for visual dynamic range
            }

            WaveformRepository.emitSample(bars)
        }

        // Pass-through raw audio data to output buffer for ExoPlayer output
        val buffer = replaceOutputBuffer(remaining)
        buffer.put(inputBuffer)
        buffer.flip()
    }
}
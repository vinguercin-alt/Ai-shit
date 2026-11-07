package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural low-latency arcade audio synthesizer.
 * Generates classic 90s action sound effects on the fly without heavy external audio files.
 */
class ArcadeSoundSynth {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isEnabled: Boolean = true

    private fun playPcm(sampleRate: Int, samples: ShortArray) {
        if (!isEnabled) return
        scope.launch {
            try {
                val bufferSize = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Sleep until finished, then release
                val durationMs = (samples.size * 1000L) / sampleRate
                Thread.sleep(durationMs + 30)
                track.release()
            } catch (_: Exception) {
                // Ignore transient audio error
            }
        }
    }

    /**
     * Fast punch / spiked claw hit
     */
    fun playPunch() {
        val sampleRate = 22050
        val durationMs = 120
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val freq = 340f - (t / (durationMs / 1000f)) * 260f // pitch drop
            val env = exp(-t * 28.0).toFloat()
            val wave = sin(2.0 * PI * freq * t).toFloat()
            val noise = ((Math.random() - 0.5) * 0.4).toFloat()
            val sample = ((wave + noise) * env * 24000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * Spiked chain whip slash / metallic whoosh
     */
    fun playChainWhip() {
        val sampleRate = 22050
        val durationMs = 200
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val freq1 = 450f + sin(t * 80.0).toFloat() * 180f
            val freq2 = 820f + cos(t * 120.0).toFloat() * 220f
            val env = (1.0f - (i.toFloat() / count)) * (1.0f - exp(-t * 50.0).toFloat())
            val metal = sin(2.0 * PI * freq1 * t).toFloat() * 0.5f +
                    sin(2.0 * PI * freq2 * t).toFloat() * 0.4f
            val sample = (metal * env * 26000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * Cape Deflect / Shield block clang
     */
    fun playCapeDeflect() {
        val sampleRate = 22050
        val durationMs = 180
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val freq = 1200f
            val env = exp(-t * 30.0).toFloat()
            val chime = sin(2.0 * PI * freq * t).toFloat() + 0.4f * sin(2.0 * PI * (freq * 1.5) * t).toFloat()
            val sample = (chime * env * 28000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * Necroplasm Blast energy surge
     */
    fun playNecroBlast() {
        val sampleRate = 22050
        val durationMs = 240
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val freq = 200f + (t * 1400f) // upward laser surge
            val env = exp(-t * 14.0).toFloat()
            val wave = (sin(2.0 * PI * freq * t) > 0.0).let { if (it) 0.7f else -0.7f } // retro square wave
            val sample = (wave * env * 22000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * Hell Nova Screen Cleanser explosion
     */
    fun playHellNova() {
        val sampleRate = 22050
        val durationMs = 500
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val bassFreq = 65f + sin(t * 20.0).toFloat() * 30f
            val env = (1.0f - (i.toFloat() / count))
            val noise = ((Math.random() - 0.5) * 0.8).toFloat()
            val bass = sin(2.0 * PI * bassFreq * t).toFloat() * 0.6f
            val sample = ((bass + noise) * env * 30000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * Demon hit / death screech
     */
    fun playDemonRoar() {
        val sampleRate = 22050
        val durationMs = 260
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val freq = 180f - (t * 90f)
            val wobble = sin(t * 60.0).toFloat() * 40f
            val env = exp(-t * 12.0).toFloat()
            val roar = sin(2.0 * PI * (freq + wobble) * t).toFloat() + ((Math.random() - 0.5) * 0.5).toFloat()
            val sample = (roar * env * 25000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * Stage victory fanfare
     */
    fun playVictory() {
        val sampleRate = 22050
        val durationMs = 600
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        val chordNotes = floatArrayOf(440f, 554.37f, 659.25f, 880f)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val noteIndex = ((t / (durationMs / 1000f)) * 4).toInt().coerceIn(0, 3)
            val freq = chordNotes[noteIndex]
            val env = (1.0f - ((t * 4) % 0.25f) / 0.25f)
            val wave = sin(2.0 * PI * freq * t).toFloat()
            val sample = (wave * env * 22000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }

    /**
     * UI button click / select
     */
    fun playMenuClick() {
        val sampleRate = 22050
        val durationMs = 50
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val freq = 750f
            val env = exp(-t * 60.0).toFloat()
            val wave = sin(2.0 * PI * freq * t).toFloat()
            val sample = (wave * env * 20000f).toInt().coerceIn(-32767, 32767)
            buffer[i] = sample.toShort()
        }
        playPcm(sampleRate, buffer)
    }
}

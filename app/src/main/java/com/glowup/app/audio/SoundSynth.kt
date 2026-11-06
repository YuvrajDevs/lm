package com.glowup.app.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** Procedurally generated ambient sounds, so nothing has to be bundled or downloaded. */
class SoundSynth(private val sound: String, private val sampleRate: Int, seed: Long = System.nanoTime()) {
    private val random = Random(seed)
    private var sampleIndex = 0L

    private var pink0 = 0.0
    private var pink1 = 0.0
    private var pink2 = 0.0
    private var brown = 0.0
    private var lowpass = 0.0
    private var deepLowpass = 0.0
    private var previousWhite = 0.0

    private var transientEnvelope = 0.0
    private val transientDecay = exp(-1.0 / (0.004 * sampleRate))

    private var chirpSamplesLeft = 0
    private var chirpLength = 1
    private var chirpRepeatsLeft = 0
    private var chirpGapLeft = 0
    private var chirpFrom = 0.0
    private var chirpTo = 0.0
    private var chirpPhase = 0.0
    private var nextBirdIn = sampleRate * 2

    fun fill(buffer: FloatArray) {
        for (i in buffer.indices) buffer[i] = next().coerceIn(-1.0, 1.0).toFloat()
    }

    private fun next(): Double {
        val t = sampleIndex.toDouble() / sampleRate
        sampleIndex++
        val white = random.nextDouble() * 2 - 1
        pink0 = 0.99765 * pink0 + white * 0.0990460
        pink1 = 0.96300 * pink1 + white * 0.2965164
        pink2 = 0.57000 * pink2 + white * 1.0526913
        val pink = (pink0 + pink1 + pink2 + white * 0.1848) * 0.11
        brown = (brown + 0.02 * white) / 1.02
        val brownOut = brown * 3.5
        val highpassed = white - previousWhite
        previousWhite = white

        return when (sound) {
            "White Noise" -> white * 0.22
            "Brown Noise" -> brownOut * 0.9
            "Night Rain" -> {
                if (random.nextDouble() < 45.0 / sampleRate) transientEnvelope += 0.15 + random.nextDouble() * 0.35
                transientEnvelope *= transientDecay
                pink * 0.55 + transientEnvelope * highpassed * 0.5
            }
            "Ocean Waves" -> {
                val swell = 0.5 + 0.5 * sin(2 * PI * t / 9.0)
                val envelope = 0.25 + 0.75 * swell * swell
                (brownOut * 0.8 + pink * 0.35) * envelope
            }
            "Soft Wind" -> {
                val gust = 0.5 + 0.5 * sin(2 * PI * t / 7.0) * sin(2 * PI * t / 13.0 + 1.3)
                val alpha = 0.01 + 0.05 * gust
                lowpass += alpha * (pink * 4 - lowpass)
                lowpass * (0.35 + 0.65 * gust)
            }
            "Crackling Fire" -> {
                if (random.nextDouble() < 12.0 / sampleRate) transientEnvelope += random.nextDouble().let { it * it } * 0.9
                transientEnvelope *= transientDecay
                brownOut * 0.45 + transientEnvelope * highpassed * 0.6
            }
            "Forest" -> {
                lowpass += 0.08 * (pink - lowpass)
                lowpass * 1.4 + bird() * 0.12
            }
            "Deep Space" -> {
                deepLowpass += 0.004 * (brownOut - deepLowpass)
                val drone = sin(2 * PI * 55.0 * t) * (0.5 + 0.5 * sin(2 * PI * t / 17.0)) +
                    sin(2 * PI * 82.41 * t) * (0.5 + 0.5 * sin(2 * PI * t / 23.0 + 2.0)) +
                    sin(2 * PI * 110.0 * t) * 0.3 * (0.5 + 0.5 * sin(2 * PI * t / 11.0))
                deepLowpass * 2.5 + drone * 0.07
            }
            else -> pink * 0.6
        }
    }

    private fun bird(): Double {
        if (chirpSamplesLeft > 0) {
            val progress = 1.0 - chirpSamplesLeft.toDouble() / chirpLength
            val frequency = chirpFrom + (chirpTo - chirpFrom) * progress
            chirpPhase += 2 * PI * frequency / sampleRate
            chirpSamplesLeft--
            val envelope = sin(PI * progress)
            return sin(chirpPhase) * envelope
        }
        if (chirpRepeatsLeft > 0) {
            if (chirpGapLeft > 0) {
                chirpGapLeft--
            } else {
                chirpRepeatsLeft--
                startChirp()
            }
            return 0.0
        }
        if (--nextBirdIn <= 0) {
            nextBirdIn = (sampleRate * (3 + random.nextDouble() * 6)).toInt()
            chirpRepeatsLeft = 1 + random.nextInt(4)
            chirpFrom = 2200 + random.nextDouble() * 1500
            chirpTo = chirpFrom + 600 + random.nextDouble() * 1400
            startChirp()
        }
        return 0.0
    }

    private fun startChirp() {
        chirpLength = (sampleRate * (0.05 + random.nextDouble() * 0.07)).toInt()
        chirpSamplesLeft = chirpLength
        chirpGapLeft = (sampleRate * (0.04 + random.nextDouble() * 0.08)).toInt()
    }
}

package at.vulkanspirit.lcarstv.audio

import kotlin.math.PI
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

enum class Sfx { FOCUS, SELECT, BACK, LAUNCH, TOGGLE, ERROR, BOOT, ALERT }

/**
 * Generates all interface sounds procedurally (16-bit mono PCM).
 * They are original synthesized tones, not samples from any show.
 */
object ToneSynth {
    const val SAMPLE_RATE = 22050

    fun samplesFor(ms: Int) = (SAMPLE_RATE * ms / 1000.0).roundToInt()

    /**
     * A sine (optionally with odd harmonics for a "brassy" klaxon tone) sweeping
     * linearly from [startHz] to [endHz], with a linear attack/release envelope.
     */
    fun tone(
        startHz: Double,
        endHz: Double = startHz,
        durationMs: Int,
        volume: Double = 0.5,
        harmonics: Boolean = false,
        attackMs: Int = 4,
        releaseMs: Int = 30,
    ): ShortArray {
        val n = samplesFor(durationMs)
        val attack = samplesFor(attackMs).coerceAtLeast(1)
        val release = samplesFor(releaseMs).coerceAtLeast(1)
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / n
            val f = startHz + (endHz - startHz) * t
            phase += 2 * PI * f / SAMPLE_RATE
            var v = sin(phase)
            if (harmonics) v = (v + sin(3 * phase) / 3 + sin(5 * phase) / 5) / 1.53
            val env = min(1.0, min(i.toDouble() / attack, (n - i).toDouble() / release))
            out[i] = (v * env * volume.coerceIn(0.0, 1.0) * Short.MAX_VALUE).roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    fun silence(ms: Int) = ShortArray(samplesFor(ms))

    fun concat(vararg parts: ShortArray): ShortArray {
        val out = ShortArray(parts.sumOf { it.size })
        var pos = 0
        for (p in parts) {
            p.copyInto(out, pos)
            pos += p.size
        }
        return out
    }

    fun build(sfx: Sfx): ShortArray = when (sfx) {
        Sfx.FOCUS -> tone(1560.0, durationMs = 28, volume = 0.18, releaseMs = 18)
        Sfx.SELECT -> concat(
            tone(1046.0, durationMs = 45, volume = 0.35),
            tone(1568.0, durationMs = 60, volume = 0.35),
        )
        Sfx.BACK -> concat(
            tone(1318.0, durationMs = 40, volume = 0.3),
            tone(880.0, durationMs = 60, volume = 0.3),
        )
        Sfx.TOGGLE -> tone(740.0, 1180.0, durationMs = 70, volume = 0.3)
        Sfx.LAUNCH -> concat(
            tone(784.0, durationMs = 50, volume = 0.32),
            tone(1046.0, durationMs = 50, volume = 0.32),
            tone(1318.0, durationMs = 50, volume = 0.32),
            tone(2093.0, 2400.0, durationMs = 160, volume = 0.25, releaseMs = 120),
        )
        Sfx.ERROR -> concat(
            tone(220.0, durationMs = 120, volume = 0.4, harmonics = true),
            silence(40),
            tone(196.0, durationMs = 160, volume = 0.4, harmonics = true),
        )
        Sfx.BOOT -> concat(
            tone(523.0, 1046.0, durationMs = 260, volume = 0.25, releaseMs = 60),
            tone(1046.0, durationMs = 70, volume = 0.3),
            tone(1568.0, durationMs = 90, volume = 0.3),
            tone(2093.0, durationMs = 220, volume = 0.22, releaseMs = 180),
        )
        // One klaxon cycle: rising "whoop" then a short gap. Looped by the sound engine.
        Sfx.ALERT -> concat(
            tone(380.0, 900.0, durationMs = 680, volume = 0.55, harmonics = true, attackMs = 20, releaseMs = 60),
            silence(320),
        )
    }
}

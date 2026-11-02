package at.vulkanspirit.lcarstv

import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.audio.ToneSynth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ToneSynthTest {
    @Test fun toneHasExpectedLengthAndFadesToSilence() {
        val t = ToneSynth.tone(1000.0, durationMs = 100, volume = 0.5)
        assertEquals(ToneSynth.samplesFor(100), t.size)
        assertTrue(t.maxOf { abs(it.toInt()) } > 10_000)
        assertTrue(abs(t.first().toInt()) < 500)
        assertTrue(abs(t.last().toInt()) < 500)
    }

    @Test fun everySoundIsAudibleAndBounded() {
        Sfx.entries.forEach { sfx ->
            val pcm = ToneSynth.build(sfx)
            assertTrue("$sfx empty", pcm.isNotEmpty())
            assertTrue("$sfx silent", pcm.any { abs(it.toInt()) > 1000 })
        }
    }

    @Test fun alertCycleIsOneSecond() {
        assertEquals(ToneSynth.SAMPLE_RATE, ToneSynth.build(Sfx.ALERT).size)
    }
}

package at.vulkanspirit.lcarstv

import at.vulkanspirit.lcarstv.data.Astro
import at.vulkanspirit.lcarstv.data.Format
import at.vulkanspirit.lcarstv.i18n.De
import at.vulkanspirit.lcarstv.i18n.En
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class AstroFormatTest {
    @Test fun stardateFollowsConvention() {
        val tngPremiereYear = ZonedDateTime.of(1987, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(41000.0, Astro.stardate(tngPremiereYear), 0.001)
        val mid2026 = ZonedDateTime.of(2026, 7, 2, 12, 0, 0, 0, ZoneOffset.UTC)
        assertEquals(80500.0, Astro.stardate(mid2026), 2.0)
        assertEquals("80500.0", Astro.formatStardate(80500.0))
    }

    @Test fun moonPhaseNearKnownNewAndFullMoons() {
        // 2024-04-08 total solar eclipse = new moon; 2024-04-23 23:49 UTC = full moon.
        val newMoon = ZonedDateTime.of(2024, 4, 8, 18, 21, 0, 0, ZoneOffset.UTC).toInstant()
        val age = Astro.moonAgeDays(newMoon)
        assertTrue("age $age", age < 1.0 || age > Astro.SYNODIC_MONTH - 1.0)
        assertTrue(Astro.moonIllumination(newMoon) < 0.05)
        val fullMoon = ZonedDateTime.of(2024, 4, 23, 23, 49, 0, 0, ZoneOffset.UTC).toInstant()
        assertTrue(Astro.moonIllumination(fullMoon) > 0.95)
        assertEquals(4, Astro.moonPhaseIndex(fullMoon))
        assertEquals("FULL MOON", En.moonPhase(4))
        assertEquals("VOLLMOND", De.moonPhase(4))
    }

    @Test fun yearProgress() {
        assertEquals(0.0, Astro.yearProgress(ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)), 1e-9)
        assertEquals(0.5, Astro.yearProgress(ZonedDateTime.of(2026, 7, 2, 12, 0, 0, 0, ZoneOffset.UTC)), 0.002)
    }

    @Test fun formatsBytesAndPercent() {
        assertEquals("512 B", Format.bytes(512))
        assertEquals("1.0 KB", Format.bytes(1024))
        assertEquals("1.5 GB", Format.bytes(1610612736))
        assertEquals("128 GB", Format.bytes(128L * 1024 * 1024 * 1024))
        assertEquals(50, Format.percent(5, 10))
        assertEquals(0, Format.percent(5, 0))
        assertEquals("1D 2H 3M", Format.duration(((24 + 2) * 60 + 3) * 60_000L))
    }

    @Test fun weatherCodes() {
        assertEquals("CLEAR SKY", En.weather(0))
        assertEquals("THUNDERSTORM", En.weather(95))
        assertEquals("UNKNOWN CONDITIONS", En.weather(1234))
        assertEquals("GEWITTER", De.weather(95))
    }
}

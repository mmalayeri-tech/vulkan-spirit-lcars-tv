package at.vulkanspirit.lcarstv.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.cos
import kotlin.math.floor

/** Pure astronomy / timekeeping helpers for the Astrometrics deck. */
object Astro {
    /**
     * A stylistic "TNG-look" stardate, not an official formula: 1000 units per year,
     * anchored so that 1 January 1987 (the year The Next Generation premiered, whose first
     * season used stardates 41xxx) is 41000.0. Present-day dates are positive five-digit values.
     */
    fun stardate(time: ZonedDateTime): Double {
        val year = time.year
        val start = LocalDate.of(year, 1, 1).atStartOfDay(time.zone)
        val end = start.plusYears(1)
        val fraction = (time.toEpochSecond() - start.toEpochSecond()).toDouble() /
            (end.toEpochSecond() - start.toEpochSecond()).toDouble()
        return 41000.0 + 1000.0 * (year - 1987) + 1000.0 * fraction
    }

    fun formatStardate(value: Double): String = String.format(Locale.US, "%.1f", value)

    fun yearProgress(time: ZonedDateTime): Double {
        val start = LocalDate.of(time.year, 1, 1).atStartOfDay(time.zone)
        val end = start.plusYears(1)
        return (time.toEpochSecond() - start.toEpochSecond()).toDouble() /
            (end.toEpochSecond() - start.toEpochSecond()).toDouble()
    }

    const val SYNODIC_MONTH = 29.530588853

    /** Reference new moon: 2000-01-06 18:14 UTC. */
    private val referenceNewMoon: Instant = ZonedDateTime.of(2000, 1, 6, 18, 14, 0, 0, ZoneOffset.UTC).toInstant()

    /** Moon age in days since the last (mean) new moon, 0 until SYNODIC_MONTH. */
    fun moonAgeDays(instant: Instant): Double {
        val days = (instant.epochSecond - referenceNewMoon.epochSecond) / 86400.0
        val age = days - SYNODIC_MONTH * floor(days / SYNODIC_MONTH)
        return age
    }

    /** Illuminated fraction 0..1. */
    fun moonIllumination(instant: Instant): Double {
        val age = moonAgeDays(instant)
        return (1 - cos(2 * Math.PI * age / SYNODIC_MONTH)) / 2
    }

    /** 0 = new, 1 = waxing crescent … 4 = full … 7 = waning crescent. */
    fun moonPhaseIndex(instant: Instant): Int {
        val age = moonAgeDays(instant)
        return ((age / SYNODIC_MONTH) * 8 + 0.5).toInt() % 8
    }
}

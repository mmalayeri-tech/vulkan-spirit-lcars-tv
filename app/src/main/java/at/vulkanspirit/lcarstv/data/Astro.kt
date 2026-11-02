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
     * A popular fan convention for "TNG-era" stardates:
     * 1000 units per year, counted from the year 2323. Present-day dates are negative.
     * This is a stylistic calculation, not an official formula.
     */
    fun stardate(time: ZonedDateTime): Double {
        val year = time.year
        val start = LocalDate.of(year, 1, 1).atStartOfDay(time.zone)
        val end = start.plusYears(1)
        val fraction = (time.toEpochSecond() - start.toEpochSecond()).toDouble() /
            (end.toEpochSecond() - start.toEpochSecond()).toDouble()
        return 1000.0 * (year - 2323) + 1000.0 * fraction
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

    fun moonPhaseName(instant: Instant): String {
        val age = moonAgeDays(instant)
        val index = ((age / SYNODIC_MONTH) * 8 + 0.5).toInt() % 8
        return listOf(
            "NEW MOON", "WAXING CRESCENT", "FIRST QUARTER", "WAXING GIBBOUS",
            "FULL MOON", "WANING GIBBOUS", "LAST QUARTER", "WANING CRESCENT",
        )[index]
    }
}

/** Short, original "space facts" rotated on the Astrometrics deck. */
val SPACE_FACTS = listOf(
    "LIGHT FROM THE SUN REACHES EARTH IN ABOUT 8 MINUTES 20 SECONDS.",
    "THE INTERNATIONAL SPACE STATION CIRCLES EARTH ROUGHLY EVERY 90 MINUTES.",
    "A DAY ON VENUS IS LONGER THAN ITS YEAR.",
    "JUPITER'S GREAT RED SPOT IS A STORM LARGER THAN EARTH.",
    "THE VOYAGER 1 PROBE IS THE MOST DISTANT HUMAN-MADE OBJECT.",
    "THE MOON DRIFTS AWAY FROM EARTH BY ABOUT 3.8 CM EACH YEAR.",
    "NEUTRON STARS CAN SPIN HUNDREDS OF TIMES PER SECOND.",
    "SATURN WOULD FLOAT IN WATER: ITS AVERAGE DENSITY IS BELOW 1 G/CM3.",
    "THE MILKY WAY CONTAINS AN ESTIMATED 100 TO 400 BILLION STARS.",
    "ALPHA CENTAURI IS THE NEAREST STAR SYSTEM, ABOUT 4.37 LIGHT-YEARS AWAY.",
)

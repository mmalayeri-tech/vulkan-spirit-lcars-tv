package at.vulkanspirit.lcarstv.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.data.Astro
import at.vulkanspirit.lcarstv.data.Presets
import at.vulkanspirit.lcarstv.data.WeatherState
import at.vulkanspirit.lcarstv.ui.components.InfoLine
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.components.LcarsPanel
import at.vulkanspirit.lcarstv.ui.components.SegmentGauge
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun AstrometricsScreen(vm: LcarsViewModel) {
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    val strings = LocalStrings.current
    val weather by vm.weather.collectAsStateWithLifecycle()
    val now = rememberNow(1000)
    var factIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(12_000)
            factIndex = (factIndex + 1) % strings.spaceFacts.size
        }
    }
    val clockFmt = remember(settings.use24h) {
        DateTimeFormatter.ofPattern(if (settings.use24h) "HH:mm" else "hh:mm a", Locale.US)
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LcarsPanel(strings.exteriorSensors, palette.primary, Modifier.weight(1f)) {
                when (val w = weather) {
                    WeatherState.Disabled -> LText(strings.weatherDisabled, size = 15.sp, color = palette.textDim, maxLines = 2)
                    WeatherState.Loading -> LText(strings.scanningLocation(strings.city(settings.location.name)), size = 15.sp, color = palette.textDim)
                    is WeatherState.Failed -> {
                        LText(strings.weatherFailed(w.httpCode), size = 15.sp, color = palette.alert)
                        LText(strings.checkNetwork, size = 13.sp, color = palette.textDim)
                    }
                    is WeatherState.Ready -> {
                        val r = w.report
                        LText(strings.city(r.location), size = 18.sp, color = palette.secondary, bold = true)
                        LText(String.format(Locale.US, "%.1f°C", r.temperatureC), size = 44.sp, color = palette.text, bold = true)
                        LText(strings.weather(r.code), size = 16.sp, color = palette.accent)
                        InfoLine(strings.highLow, String.format(Locale.US, "%.0f° / %.0f°", r.maxC, r.minC))
                        InfoLine(strings.humidity, "${r.humidity}%")
                        InfoLine(strings.wind, String.format(Locale.US, "%.0f KM/H", r.windKmh))
                        InfoLine(strings.sunriseSunset, "${r.sunrise} / ${r.sunset}")
                    }
                }
            }
            LcarsPanel(strings.temporalReference, palette.tertiary, Modifier.weight(1f)) {
                val zones = Presets.zones.filter { it.zoneId in settings.worldClocks }
                zones.forEach { z ->
                    val t = runCatching { now.withZoneSameInstant(ZoneId.of(z.zoneId)) }.getOrNull() ?: return@forEach
                    val dayDiff = ChronoUnit.DAYS.between(now.toLocalDate(), t.toLocalDate())
                    val suffix = when {
                        dayDiff > 0 -> "  ${strings.nextDay}"
                        dayDiff < 0 -> "  ${strings.previousDay}"
                        else -> ""
                    }
                    InfoLine(strings.city(z.label), t.format(clockFmt) + suffix)
                }
                if (zones.isEmpty()) LText(strings.noWorldClocks, size = 15.sp, color = palette.textDim)
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OrbitDisplay(Modifier.fillMaxWidth().height(120.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoonGlyph(Astro.moonIllumination(now.toInstant()).toFloat(), Astro.moonAgeDays(now.toInstant()) < Astro.SYNODIC_MONTH / 2, Modifier.size(40.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        LText(strings.moonPhase(Astro.moonPhaseIndex(now.toInstant())), size = 15.sp, color = palette.text)
                        LText("${strings.illumination} ${(Astro.moonIllumination(now.toInstant()) * 100).toInt()}%", size = 13.sp, color = palette.textDim)
                    }
                }
                InfoLine(strings.stardate, Astro.formatStardate(Astro.stardate(now)))
                InfoLine(strings.solarOrbit, "${(Astro.yearProgress(now) * 100).toInt()}%")
                SegmentGauge(Astro.yearProgress(now).toFloat(), palette.secondary, warnAbove = 2f)
            }
        }
        LText(strings.spaceFacts[factIndex % strings.spaceFacts.size], size = 15.sp, color = palette.accent, maxLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LcarsButton(
                strings.location(strings.city(settings.location.name)), palette.primary,
                {
                    vm.updateSettings { it.copy(weatherLocation = (it.weatherLocation + 1) % Presets.locations.size) }
                },
                Modifier.width(240.dp), height = 36.dp, textSize = 15,
            )
            LcarsButton(strings.refreshScan, palette.tertiary, { vm.refreshWeather() }, Modifier.width(150.dp), height = 36.dp, textSize = 15)
            LcarsButton(
                strings.sensors(settings.weatherEnabled), palette.secondary,
                { vm.updateSettings { it.copy(weatherEnabled = !it.weatherEnabled) } },
                Modifier.width(150.dp), height = 36.dp, textSize = 15,
            )
        }
    }
}

/** Stylised inner-system orbit display (not to scale). */
@Composable
private fun OrbitDisplay(modifier: Modifier) {
    val palette = LocalPalette.current
    val motion = LocalLcarsSettings.current.motion
    val t by rememberInfiniteTransition(label = "orbit").animateFloat(
        0f, 360f, infiniteRepeatable(tween(60_000, easing = LinearEasing)), label = "t",
    )
    val time = if (motion) t else 120f
    val planets = listOf(4.15f to palette.textDim, 1.62f to palette.accent, 1f to palette.tertiary, 0.53f to palette.alert, 0.084f to palette.highlight)
    Canvas(modifier) {
        val c = center
        val maxR = min(size.width / 2, size.height / 2) - 4f
        drawCircle(palette.primary, 7f, c)
        planets.forEachIndexed { i, (speed, color) ->
            val r = maxR * (i + 1) / planets.size
            drawCircle(color.copy(alpha = 0.3f), r, c, style = Stroke(1.2f))
            val a = Math.toRadians((time * speed + i * 67).toDouble())
            drawCircle(color, 3.5f + i * 0.6f, Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat()))
        }
    }
}

/** Draws the moon with its lit fraction. */
@Composable
private fun MoonGlyph(illumination: Float, waxing: Boolean, modifier: Modifier) {
    val palette = LocalPalette.current
    Canvas(modifier) {
        val r = size.minDimension / 2
        val c = center
        drawCircle(Color(0xFF222222), r, c)
        // Terminator ellipse: x-radius goes from r (new/full) through 0 (quarter).
        val k = 1 - 2 * illumination
        val path = Path().apply {
            val half = Rect(c.x - r, c.y - r, c.x + r, c.y + r)
            arcTo(half, -90f, if (waxing) 180f else -180f, false)
            val ex = r * k
            arcTo(Rect(c.x - kotlin.math.abs(ex), c.y - r, c.x + kotlin.math.abs(ex), c.y + r), 90f,
                if ((ex > 0) == waxing) -180f else 180f, false)
            close()
        }
        drawPath(path, palette.accent)
    }
}

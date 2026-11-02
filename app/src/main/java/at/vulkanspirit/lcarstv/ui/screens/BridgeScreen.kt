package at.vulkanspirit.lcarstv.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.data.Astro
import at.vulkanspirit.lcarstv.data.Format
import at.vulkanspirit.lcarstv.data.WeatherState
import at.vulkanspirit.lcarstv.ui.Section
import at.vulkanspirit.lcarstv.ui.components.AppTile
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun BridgeScreen(vm: LcarsViewModel, onNavigate: (Section) -> Unit) {
    val settings = LocalLcarsSettings.current
    val palette = LocalPalette.current
    val context = LocalContext.current
    val apps by vm.apps.collectAsStateWithLifecycle()
    val system by vm.system.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val redAlert by vm.redAlert.collectAsStateWithLifecycle()
    val now = rememberNow(1000)

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshSystem()
            delay(15_000)
        }
    }

    val timeFmt = remember(settings.use24h) {
        DateTimeFormatter.ofPattern(if (settings.use24h) "HH:mm:ss" else "hh:mm:ss a", Locale.US)
    }
    val dateFmt = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val favorites = remember(apps, settings.favorites) {
        settings.favorites.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                LText("WELCOME ABOARD", size = 18.sp, color = palette.secondary, bold = true)
                LText("U.S.S. ${settings.shipName}", size = 38.sp, color = palette.primary, bold = true)
                LText(settings.registry, size = 18.sp, color = palette.tertiary)
                Spacer(Modifier.height(4.dp))
                LText(now.format(timeFmt), size = 54.sp, color = palette.text, bold = true)
                LText(now.format(dateFmt).uppercase(), size = 18.sp, color = palette.accent)
                LText("STARDATE ${Astro.formatStardate(Astro.stardate(now))}", size = 16.sp, color = palette.textDim)
            }
            SensorSweep(Modifier.size(170.dp))
        }

        // System overview strip
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val storage = system.storage.firstOrNull()
            StatBlock("PROGRAMS", apps.size.toString(), palette.primary, Modifier.weight(1f))
            StatBlock(
                "STORAGE",
                storage?.let { "${Format.percent(it.total - it.free, it.total)}%" } ?: "—",
                palette.secondary, Modifier.weight(1f),
            )
            StatBlock(
                "MEMORY",
                if (system.memTotal > 0) "${Format.percent(system.memTotal - system.memAvail, system.memTotal)}%" else "—",
                palette.tertiary, Modifier.weight(1f),
            )
            StatBlock("NETWORK", if (system.connected) system.networkType else "OFFLINE", palette.accent, Modifier.weight(1f))
            StatBlock(
                "EXTERIOR",
                (weather as? WeatherState.Ready)?.report?.let { "${it.temperatureC.toInt()}°C" } ?: "—",
                palette.highlight, Modifier.weight(1f),
            )
            StatBlock(
                "CONDITION",
                if (redAlert) "RED" else "GREEN",
                if (redAlert) palette.alert else Color(0xFF66CC66), Modifier.weight(1f),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            LText("PRIORITY CHANNELS", size = 18.sp, color = palette.secondary, bold = true)
            Spacer(Modifier.weight(1f))
            LcarsButton("HOLODECK", palette.primary, { onNavigate(Section.HOLODECK) }, Modifier.width(120.dp), height = 32.dp, textSize = 14)
            Spacer(Modifier.width(8.dp))
            LcarsButton(
                "ANDROID SETTINGS", palette.tertiary,
                { vm.openSystemSettings(context, Settings.ACTION_SETTINGS) },
                Modifier.width(160.dp), height = 32.dp, textSize = 14,
            )
        }

        if (favorites.isEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LText(
                    "NO FAVORITES ASSIGNED. HOLD OK ON ANY APP, OR USE THE COMPUTER CORE.",
                    Modifier.weight(1f),
                    size = 16.sp,
                    color = palette.textDim,
                )
                LcarsButton("COMPUTER CORE", palette.secondary, { onNavigate(Section.COMPUTER_CORE) }, Modifier.width(150.dp), height = 32.dp, textSize = 14)
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                itemsIndexed(favorites, key = { _, a -> a.packageName }) { i, app ->
                    AppTile(
                        app = app,
                        isFavorite = true,
                        colorIndex = i,
                        onLaunch = { vm.launch(context, app) },
                        onToggleFavorite = { vm.toggleFavorite(app.packageName) },
                        modifier = Modifier.width(160.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
        LText(label, size = 13.sp, color = palette.textDim, modifier = Modifier.padding(top = 3.dp))
        LText(value, size = 22.sp, color = color, bold = true, align = TextAlign.Start)
    }
}

/** A rotating sensor sweep with blips: one infinite transition, cheap to draw. */
@Composable
fun SensorSweep(modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    val motion = LocalLcarsSettings.current.motion
    val transition = rememberInfiniteTransition(label = "sweep")
    val angle by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "angle")
    val pulse by transition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "pulse")
    val a = if (motion) angle else 45f
    val p = if (motion) pulse else 1f
    val blips = remember { List(7) { Pair((it * 53f + 20f) % 360f, 0.25f + (it * 0.11f) % 0.7f) } }
    Canvas(modifier) {
        val r = min(size.width, size.height) / 2
        val c = center
        for (i in 1..4) drawCircle(palette.tertiary.copy(alpha = 0.55f), r * i / 4, c, style = Stroke(2f))
        drawLine(palette.tertiary.copy(alpha = 0.4f), Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1.5f)
        drawLine(palette.tertiary.copy(alpha = 0.4f), Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1.5f)
        rotate(a, c) {
            for (k in 0 until 24) {
                drawLine(
                    palette.primary.copy(alpha = (1f - k / 24f) * 0.5f),
                    c,
                    Offset(c.x + r * cos(Math.toRadians(-k * 2.0)).toFloat(), c.y + r * sin(Math.toRadians(-k * 2.0)).toFloat()),
                    3f,
                )
            }
        }
        blips.forEach { (deg, dist) ->
            val rad = Math.toRadians(deg.toDouble())
            drawCircle(
                palette.accent.copy(alpha = p),
                5f,
                Offset(c.x + r * dist * cos(rad).toFloat(), c.y + r * dist * sin(rad).toFloat()),
            )
        }
        drawCircle(palette.primary, 6f * p + 2f, c)
    }
}

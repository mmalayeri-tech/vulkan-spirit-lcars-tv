package at.vulkanspirit.lcarstv.ui.screens

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.Astro
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.data.WeatherState
import at.vulkanspirit.lcarstv.ui.Section
import at.vulkanspirit.lcarstv.ui.components.AppCard
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalRedAlert
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
import at.vulkanspirit.lcarstv.ui.theme.LocalWeather
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * The home deck, built like the Apple TV home screen inside an LCARS frame: a "top shelf" that
 * previews whatever app has focus (its artwork, colour and category) above rows of app cards —
 * priority channels, recently used, then one row per category — and a row of station controls.
 * With nothing focused yet, the shelf shows the station: name, clock, stardate, weather.
 */
@Composable
fun BridgeScreen(vm: LcarsViewModel, onNavigate: (Section) -> Unit) {
    val settings = LocalLcarsSettings.current
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val apps by vm.apps.collectAsStateWithLifecycle()
    val loading by vm.appsLoading.collectAsStateWithLifecycle()
    var hero by remember { mutableStateOf<InstalledApp?>(null) }

    val byPackage = remember(apps) { apps.associateBy { it.packageName } }
    val favorites = remember(byPackage, settings.favorites) { settings.favorites.mapNotNull(byPackage::get) }
    val recents = remember(byPackage, settings.recents) { settings.recents.mapNotNull(byPackage::get) }
    val groups = remember(apps) {
        listOf(AppGroup.VIDEO, AppGroup.MUSIC, AppGroup.GAMES, AppGroup.COMMUNICATION, AppGroup.OTHER, AppGroup.SYSTEM)
            .map { g -> g to apps.filter { it.group == g }.sortedByDescending { it.isTvApp } }
            .filter { it.second.isNotEmpty() }
    }

    Column(Modifier.fillMaxSize()) {
        TopShelf(hero, Modifier.fillMaxWidth().height(146.dp))
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
        ) {
            if (favorites.isNotEmpty()) {
                item(key = "fav") { AppRow(vm, strings.rowFavorites, favorites, settings.favorites) { hero = it } }
            } else if (apps.isNotEmpty()) {
                item(key = "fav-hint") {
                    LText(strings.noFavoritesHint, Modifier.padding(start = 6.dp, top = 4.dp, bottom = 2.dp), size = 14.sp, color = palette.textDim)
                }
            }
            if (recents.isNotEmpty()) {
                item(key = "recent") { AppRow(vm, strings.rowRecent, recents, settings.favorites) { hero = it } }
            }
            if (apps.isEmpty()) {
                item(key = "empty") {
                    LText(if (loading) strings.scanning else strings.noHoloApps, Modifier.padding(16.dp), size = 20.sp, color = palette.textDim)
                }
            }
            groups.forEach { (group, list) ->
                item(key = "g-${group.name}") { AppRow(vm, strings.groupRow(group), list, settings.favorites) { hero = it } }
            }
            item(key = "station") {
                Column(Modifier.padding(top = 4.dp)) {
                    RowTitle(strings.rowStation)
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val clear = { hero = null }
                        StationButton(strings.deckSearch, palette.primary, clear) { onNavigate(Section.SEARCH) }
                        StationButton(strings.deckHolodeck, palette.secondary, clear) { onNavigate(Section.HOLODECK) }
                        StationButton(strings.deckRedAlert, palette.alert, clear) { onNavigate(Section.RED_ALERT) }
                        StationButton(strings.deckSettings, palette.tertiary, clear) { onNavigate(Section.SETTINGS) }
                        StationButton(strings.androidSettings, palette.accent, clear) { vm.openSystemSettings(context, Settings.ACTION_SETTINGS) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationButton(text: String, color: Color, onFocus: () -> Unit, onClick: () -> Unit) {
    LcarsButton(text, color, onClick, Modifier.width(150.dp), height = 40.dp, textSize = 15, onFocused = onFocus)
}

@Composable
private fun RowTitle(text: String) {
    val palette = LocalPalette.current
    Row(Modifier.padding(start = 6.dp, top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 18.dp, height = 12.dp).clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)).background(palette.secondary))
        Spacer(Modifier.width(6.dp))
        LText(text, size = 15.sp, color = palette.secondary, bold = true)
    }
}

@Composable
private fun AppRow(
    vm: LcarsViewModel,
    title: String,
    apps: List<InstalledApp>,
    favorites: List<String>,
    onFocused: (InstalledApp) -> Unit,
) {
    val context = LocalContext.current
    Column {
        RowTitle(title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppCard(
                    app = app,
                    isFavorite = app.packageName in favorites,
                    onClick = { vm.launch(context, app) },
                    onLongClick = { vm.showOptions(app) },
                    width = 148.dp,
                    onFocused = onFocused,
                )
            }
        }
    }
}

/** The top shelf: the focused app's artwork and colour, or the station overview. */
@Composable
private fun TopShelf(app: InstalledApp?, modifier: Modifier) {
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    AnimatedContent(
        targetState = app,
        contentKey = { it?.packageName },
        transitionSpec = {
            if (settings.motion) fadeIn(tween(280)) togetherWith fadeOut(tween(180)) else fadeIn(tween(0)) togetherWith fadeOut(tween(0))
        },
        label = "shelf",
        modifier = modifier,
    ) { target ->
        val accent = target?.accent?.let { Color(it) } ?: palette.primary
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp, topEnd = 16.dp, bottomEnd = 16.dp))
                .background(
                    Brush.horizontalGradient(
                        0f to palette.panel,
                        0.45f to lerp(palette.panel, accent, 0.18f),
                        1f to lerp(Color.Black, accent, 0.55f),
                    ),
                ),
        ) {
            // LCARS cap on the left edge of the shelf.
            Box(Modifier.width(16.dp).fillMaxHeight().background(accent))
            if (target == null) StationShelf() else AppShelf(target, accent)
        }
    }
}

@Composable
private fun AppShelf(app: InstalledApp, accent: Color) {
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val settings = LocalLcarsSettings.current
    Row(Modifier.fillMaxSize().padding(start = 34.dp, end = 18.dp, top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            LText(strings.groupRow(app.group), size = 15.sp, color = lerp(accent, Color.White, 0.35f), bold = true)
            LText(app.label.uppercase(), size = 42.sp, color = palette.text, bold = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.packageName in settings.favorites) {
                    LText(
                        strings.pinnedBadge,
                        Modifier.clip(RoundedCornerShape(10.dp)).background(palette.highlight).padding(horizontal = 10.dp),
                        size = 13.sp,
                        color = Color.Black,
                        bold = true,
                    )
                    Spacer(Modifier.width(10.dp))
                }
                LText(if (app.isTvApp) strings.pressOk else strings.phoneApp, size = 14.sp, color = palette.textDim)
            }
        }
        Box(
            Modifier
                .fillMaxHeight()
                .aspectRatio(16f / 9f)
                .shadow(18.dp, RoundedCornerShape(14.dp), ambientColor = accent, spotColor = accent)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(lerp(accent, Color.Black, 0.2f), lerp(accent, Color.Black, 0.7f)))),
            contentAlignment = Alignment.Center,
        ) {
            when {
                app.banner != null -> Image(app.banner, app.label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                app.icon != null -> Image(app.icon, app.label, Modifier.fillMaxHeight(0.6f).aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun StationShelf() {
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val settings = LocalLcarsSettings.current
    val vmWeather = LocalWeather.current
    val redAlert = LocalRedAlert.current
    val now = rememberNow(1000)
    val timeFmt = remember(settings.use24h) {
        DateTimeFormatter.ofPattern(if (settings.use24h) "HH:mm" else "hh:mm a", Locale.US)
    }
    val dateFmt = remember(strings) { DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", strings.locale) }
    Row(Modifier.fillMaxSize().padding(start = 34.dp, end = 14.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            LText(strings.welcomeAboard, size = 15.sp, color = palette.secondary, bold = true)
            LText(settings.shipName, size = 40.sp, color = palette.primary, bold = true)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LText(settings.registry, size = 16.sp, color = palette.tertiary)
                Spacer(Modifier.width(14.dp))
                LText(
                    if (redAlert) strings.conditionRed else strings.conditionGreen,
                    size = 16.sp,
                    color = if (redAlert) palette.alert else Color(0xFF66CC66),
                    bold = true,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            LText(now.format(timeFmt), size = 46.sp, color = palette.text, bold = true)
            LText(now.format(dateFmt).uppercase(strings.locale), size = 15.sp, color = palette.accent)
            val report = (vmWeather as? WeatherState.Ready)?.report
            LText(
                listOfNotNull(
                    "${strings.stardate} ${Astro.formatStardate(Astro.stardate(now))}",
                    report?.let { "${strings.city(it.location)} ${it.temperatureC.toInt()}°C ${strings.weather(it.code)}" },
                ).joinToString("  ·  "),
                size = 14.sp,
                color = palette.textDim,
            )
        }
        Spacer(Modifier.width(14.dp))
        SensorSweep(Modifier.size(104.dp))
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

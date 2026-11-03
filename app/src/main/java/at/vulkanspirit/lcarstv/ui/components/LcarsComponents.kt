package at.vulkanspirit.lcarstv.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalSfx
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * The classic LCARS "elbow": a vertical sidebar block that curves into a horizontal bar.
 * [flipVertical] produces the lower elbow.
 */
class ElbowShape(
    private val sidebarWidth: Dp,
    private val armHeight: Dp,
    private val outerRadius: Dp,
    private val innerRadius: Dp,
    private val flipVertical: Boolean = false,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val s = with(density) { sidebarWidth.toPx() }
        val a = with(density) { armHeight.toPx() }
        val ro = with(density) { outerRadius.toPx() }
        val ri = with(density) { innerRadius.toPx() }
        val w = size.width
        val h = size.height
        val p = Path()
        if (!flipVertical) {
            p.moveTo(ro, 0f)
            p.lineTo(w, 0f)
            p.lineTo(w, a)
            p.lineTo(s + ri, a)
            p.arcTo(Rect(s, a, s + 2 * ri, a + 2 * ri), 270f, -90f, false)
            p.lineTo(s, h)
            p.lineTo(0f, h)
            p.lineTo(0f, ro)
            p.arcTo(Rect(0f, 0f, 2 * ro, 2 * ro), 180f, 90f, false)
        } else {
            p.moveTo(ro, h)
            p.lineTo(w, h)
            p.lineTo(w, h - a)
            p.lineTo(s + ri, h - a)
            p.arcTo(Rect(s, h - a - 2 * ri, s + 2 * ri, h - a), 90f, 90f, false)
            p.lineTo(s, 0f)
            p.lineTo(0f, 0f)
            p.lineTo(0f, h - ro)
            p.arcTo(Rect(0f, h - 2 * ro, 2 * ro, h), 180f, -90f, false)
        }
        p.close()
        return Outline.Generic(p)
    }
}

/** Brightens a colour toward white for the focused state. */
fun Color.focused(): Color = lerp(this, Color.White, 0.45f)

/**
 * The basic remote-friendly LCARS control. OK/Enter activates; focus brightens the block,
 * draws a white edge and (motion permitting) scales up slightly.
 */
@Composable
fun LcarsButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(percent = 50),
    height: Dp = 44.dp,
    textSize: Int = 18,
    textAlign: TextAlign = TextAlign.End,
    selected: Boolean = false,
    sound: Sfx? = Sfx.SELECT,
    onLongClick: (() -> Unit)? = null,
    onFocused: () -> Unit = {},
) {
    val sfx = LocalSfx.current
    val settings = LocalLcarsSettings.current
    val palette = LocalPalette.current
    Surface(
        onClick = {
            sound?.let(sfx)
            onClick()
        },
        onLongClick = onLongClick,
        modifier = modifier
            .height(height)
            .onFocusChanged {
                if (it.isFocused) {
                    sfx(Sfx.FOCUS)
                    onFocused()
                }
            },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = color,
            contentColor = Color.Black,
            focusedContainerColor = color.focused(),
            focusedContentColor = Color.Black,
            pressedContainerColor = Color.White,
            pressedContentColor = Color.Black,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (settings.motion) 1.04f else 1f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, palette.focus)),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = if (textAlign == TextAlign.Center) 4.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Box(
                    Modifier
                        .width(10.dp)
                        .fillMaxHeight(0.6f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black),
                )
                Spacer(Modifier.width(8.dp))
            }
            LText(
                text = text,
                modifier = Modifier.weight(1f),
                size = textSize.sp,
                color = Color.Black,
                bold = true,
                align = textAlign,
            )
        }
    }
}

/** A settings-style row: label on the left, current value on the right. */
@Composable
fun LcarsRow(
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sound: Sfx? = Sfx.TOGGLE,
) {
    val sfx = LocalSfx.current
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    Surface(
        onClick = {
            sound?.let(sfx)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .onFocusChanged { if (it.isFocused) sfx(Sfx.FOCUS) },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(topStart = 23.dp, bottomStart = 23.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = palette.panel,
            contentColor = palette.text,
            focusedContainerColor = color,
            focusedContentColor = Color.Black,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = if (settings.motion) 1.02f else 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(2.dp, color.copy(alpha = 0.6f))),
            focusedBorder = Border(BorderStroke(3.dp, palette.focus)),
        ),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(color))
            Spacer(Modifier.width(14.dp))
            LText(label, Modifier.weight(1f), size = 18.sp, color = androidx.tv.material3.LocalContentColor.current)
            LText(value, size = 18.sp, bold = true, color = androidx.tv.material3.LocalContentColor.current)
        }
    }
}

/** A titled, non-focusable information panel with a coloured cap. */
@Composable
fun LcarsPanel(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val palette = LocalPalette.current
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(28.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(topStart = 11.dp, bottomStart = 11.dp))
                    .background(color),
            )
            Spacer(Modifier.width(6.dp))
            LText(title, size = 18.sp, color = color, bold = true)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.weight(1f).height(22.dp).background(color))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 8.dp)
                .background(palette.panel.copy(alpha = 0.6f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) { content() }
    }
}

@Composable
fun InfoLine(label: String, value: String, valueColor: Color = LocalPalette.current.text) {
    Row(Modifier.fillMaxWidth()) {
        LText(label, size = 15.sp, color = LocalPalette.current.textDim)
        Spacer(Modifier.width(10.dp))
        LText(value, Modifier.weight(1f), size = 15.sp, color = valueColor, align = TextAlign.End)
    }
}

/** Segmented LCARS bar gauge (0..1). */
@Composable
fun SegmentGauge(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    segments: Int = 24,
    warnAbove: Float = 0.85f,
) {
    val palette = LocalPalette.current
    val motion = LocalLcarsSettings.current.motion
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(if (motion) 900 else 0),
        label = "gauge",
    )
    Canvas(modifier.height(16.dp).fillMaxWidth()) {
        val gap = 3.dp.toPx()
        val segW = (size.width - gap * (segments - 1)) / segments
        val lit = (animated * segments).toInt()
        for (i in 0 until segments) {
            val c = when {
                i >= lit -> palette.panel.copy(alpha = 1f).let { lerp(it, color, 0.18f) }
                i.toFloat() / segments >= warnAbove -> palette.alert
                else -> color
            }
            drawRect(c, topLeft = Offset(i * (segW + gap), 0f), size = Size(segW, size.height))
        }
    }
}

/** Decorative LCARS number cascade. Static when motion is disabled. */
@Composable
fun DataCascade(rows: Int, columns: Int, modifier: Modifier = Modifier, textSize: Int = 12) {
    val palette = LocalPalette.current
    val motion = LocalLcarsSettings.current.motion
    val cells = remember(rows, columns) {
        mutableStateListOf<String>().apply { repeat(rows * columns) { add(randomCode()) } }
    }
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(motion) {
        while (motion) {
            delay(180)
            cells[Random.nextInt(cells.size)] = randomCode()
            tick++
        }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        for (r in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (c in 0 until columns) {
                    val i = r * columns + c
                    val color = if ((i + tick / 6) % 7 == 0) palette.focus else palette.pick(i + r)
                    LText(cells[i], size = textSize.sp, color = color)
                }
            }
        }
    }
}

private fun randomCode(): String = when (Random.nextInt(4)) {
    0 -> Random.nextInt(10, 99).toString()
    1 -> Random.nextInt(100, 999).toString()
    2 -> Random.nextInt(1000, 9999).toString()
    else -> "${Random.nextInt(10, 99)}-${Random.nextInt(100, 999)}"
}

/**
 * An Apple TV-style app card inside the LCARS look: a rounded 16:9 tile that lifts, glows in the
 * app's own colour and catches a light sweep when focused; the name appears underneath only while
 * focused. Uses the app's TV banner when it has one, else its icon on a gradient of its colour.
 * OK launches, holding OK opens the options sheet.
 */
@Composable
fun AppCard(
    app: InstalledApp,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 168.dp,
    onFocused: (InstalledApp) -> Unit = {},
) {
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    val strings = LocalStrings.current
    val sfx = LocalSfx.current
    var focused by remember { mutableStateOf(false) }
    val accent = app.accent?.let { Color(it) } ?: palette.pick(app.packageName.hashCode())
    val sweep = remember { Animatable(1f) }
    LaunchedEffect(focused) {
        if (focused && settings.motion) {
            sweep.snapTo(0f)
            sweep.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
        }
    }
    Column(if (width.isSpecified) modifier.width(width) else modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .onFocusChanged {
                    if (it.isFocused && !focused) {
                        sfx(Sfx.FOCUS)
                        onFocused(app)
                    }
                    focused = it.isFocused
                },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = palette.panel,
                focusedContainerColor = palette.panel,
                pressedContainerColor = palette.panel,
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = if (settings.motion) 1.12f else 1.04f),
            glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow(accent.copy(alpha = 0.75f), 16.dp)),
            border = ClickableSurfaceDefaults.border(
                border = Border(BorderStroke(1.dp, accent.copy(alpha = 0.35f)), shape = RoundedCornerShape(12.dp)),
                focusedBorder = Border(BorderStroke(2.dp, palette.focus.copy(alpha = 0.9f)), shape = RoundedCornerShape(12.dp)),
            ),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (focused) {
                            // Static top sheen plus a one-off diagonal light sweep, like tvOS.
                            drawRect(Brush.verticalGradient(0f to Color.White.copy(alpha = 0.16f), 0.5f to Color.Transparent))
                            val x = -size.width + sweep.value * size.width * 2.5f
                            drawRect(
                                Brush.linearGradient(
                                    0f to Color.Transparent,
                                    0.5f to Color.White.copy(alpha = if (sweep.value < 1f) 0.22f else 0f),
                                    1f to Color.Transparent,
                                    start = Offset(x, 0f),
                                    end = Offset(x + size.width * 0.6f, size.height),
                                ),
                            )
                        }
                    },
            ) {
                if (app.banner != null) {
                    Image(app.banner, contentDescription = app.label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(lerp(accent, Color.Black, 0.25f), lerp(accent, Color.Black, 0.75f)))),
                    )
                    Column(
                        Modifier.fillMaxSize().padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        app.icon?.let { Image(it, contentDescription = null, Modifier.fillMaxHeight(0.58f).aspectRatio(1f)) }
                        LText(app.label.uppercase(), size = 13.sp, color = Color.White, bold = true, align = TextAlign.Center)
                    }
                }
                if (isFavorite) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(width = 22.dp, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(palette.highlight),
                    )
                }
                if (!app.isTvApp) {
                    LText(
                        strings.phoneTag,
                        Modifier
                            .align(Alignment.TopStart)
                            .padding(5.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 4.dp),
                        size = 10.sp,
                        color = palette.alert,
                        bold = true,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(22.dp).padding(top = 6.dp), contentAlignment = Alignment.Center) {
            androidx.compose.animation.AnimatedVisibility(focused, enter = fadeIn(tween(160)), exit = fadeOut(tween(120))) {
                LText(app.label.uppercase(), size = 13.sp, color = palette.focus, align = TextAlign.Center)
            }
        }
    }
}

/** The LCARS on-screen keypad: everything is reachable with the D-pad, no keyboard needed. */
@Composable
fun LcarsKeypad(
    onKey: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    extraKey: Pair<String, () -> Unit>? = null,
    trailing: @Composable () -> Unit = {},
) {
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val umlauts = strings.locale.language == "de"
    val rows = listOf("ABCDEFG", "HIJKLMN", "OPQRSTU", "VWXYZ-.", "1234567", if (umlauts) "890ÄÖÜ" else "890:")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        rows.forEachIndexed { r, keys ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                keys.forEach { k ->
                    LcarsButton(
                        text = k.toString(),
                        color = palette.pick(r),
                        onClick = { onKey(k.toString()) },
                        modifier = Modifier.width(38.dp),
                        height = 34.dp,
                        shape = RoundedCornerShape(8.dp),
                        textAlign = TextAlign.Center,
                        sound = Sfx.FOCUS,
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            LcarsButton(strings.keySpace, palette.accent, { onKey(" ") }, Modifier.width(124.dp), height = 34.dp, shape = RoundedCornerShape(8.dp), textAlign = TextAlign.Center, textSize = 14)
            LcarsButton(strings.keyDelete, palette.secondary, onDelete, Modifier.width(80.dp), height = 34.dp, shape = RoundedCornerShape(8.dp), textAlign = TextAlign.Center, textSize = 14, sound = Sfx.BACK)
            LcarsButton(strings.keyClear, palette.alert, onClear, Modifier.width(78.dp), height = 34.dp, shape = RoundedCornerShape(8.dp), textAlign = TextAlign.Center, textSize = 14, sound = Sfx.BACK)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            trailing()
            extraKey?.let { (label, action) ->
                LcarsButton(label, palette.primary, action, Modifier.width(166.dp), height = 34.dp, textAlign = TextAlign.Center, textSize = 14)
            }
        }
    }
}

/** A plain coloured bar with no focus. */
@Composable
fun Bar(color: Color, modifier: Modifier = Modifier, shape: Shape = RectangleShape) {
    Box(modifier.clip(shape).background(color))
}

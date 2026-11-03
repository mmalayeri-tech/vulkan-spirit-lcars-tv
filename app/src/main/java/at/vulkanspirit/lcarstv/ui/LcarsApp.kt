package at.vulkanspirit.lcarstv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.Astro
import at.vulkanspirit.lcarstv.i18n.Strings
import at.vulkanspirit.lcarstv.ui.components.Bar
import at.vulkanspirit.lcarstv.ui.components.DataCascade
import at.vulkanspirit.lcarstv.ui.components.ElbowShape
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.screens.AstrometricsScreen
import at.vulkanspirit.lcarstv.ui.screens.BridgeScreen
import at.vulkanspirit.lcarstv.ui.screens.CommunicationsScreen
import at.vulkanspirit.lcarstv.ui.screens.EngineeringScreen
import at.vulkanspirit.lcarstv.ui.screens.HolodeckScreen
import at.vulkanspirit.lcarstv.ui.screens.RedAlertOverlay
import at.vulkanspirit.lcarstv.ui.screens.RedAlertScreen
import at.vulkanspirit.lcarstv.ui.screens.SearchScreen
import at.vulkanspirit.lcarstv.ui.screens.SettingsScreen
import at.vulkanspirit.lcarstv.ui.screens.rememberNow
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LcarsPalettes
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalSfx
import at.vulkanspirit.lcarstv.ui.theme.LocalRedAlert
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
import at.vulkanspirit.lcarstv.ui.theme.LocalWeather
import kotlinx.coroutines.delay

enum class Section(val code: String) {
    BRIDGE("01-1701"),
    SEARCH("02-3341"),
    COMMUNICATIONS("03-4471"),
    HOLODECK("04-0815"),
    ENGINEERING("05-2260"),
    ASTROMETRICS("06-9912"),
    RED_ALERT("07-0001"),
    SETTINGS("08-5150");

    fun title(s: Strings): String = when (this) {
        BRIDGE -> s.deckBridge
        SEARCH -> s.deckSearch
        COMMUNICATIONS -> s.deckCommunications
        HOLODECK -> s.deckHolodeck
        ENGINEERING -> s.deckEngineering
        ASTROMETRICS -> s.deckAstrometrics
        RED_ALERT -> s.deckRedAlert
        SETTINGS -> s.deckSettings
    }

    /** The deck number shown on the folded rail. */
    val number: String get() = code.substringBefore('-')
}

private val RailWidth = 58.dp
private val SidebarWidth = 176.dp
private val ElbowExtra = 30.dp
private val ArmHeight = 16.dp
private val TopHeight = 58.dp
private val BottomHeight = 38.dp

@Composable
fun LcarsApp(vm: LcarsViewModel, isHomeMode: () -> Boolean, onExit: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val redAlert by vm.redAlert.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val optionsApp by vm.options.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val strings = remember(settings.language) { Strings.forLanguage(settings.language) }
    val basePalette = LcarsPalettes.forTheme(settings.theme)
    val palette = if (redAlert) LcarsPalettes.redAlert else basePalette

    var section by rememberSaveable { mutableStateOf(Section.BRIDGE) }
    var focusContent by remember { mutableStateOf(false) }
    var navFocused by remember { mutableStateOf(false) }
    val navRequesters = remember { Section.entries.associateWith { FocusRequester() } }
    val contentRequester = remember { FocusRequester() }

    fun focusNav(s: Section) = runCatching { navRequesters.getValue(s).requestFocus() }

    fun navigate(target: Section, moveFocusIntoContent: Boolean = true) {
        if (target != section) vm.say(target.title(strings).lowercase().replaceFirstChar { it.titlecase() })
        section = target
        focusContent = moveFocusIntoContent
    }

    LaunchedEffect(Unit) {
        vm.play(Sfx.BOOT)
        vm.say(strings.voiceWelcome(settings.shipName))
        // Start like Apple TV: focus on the home rows, rail folded. Fall back to the rail.
        delay(450)
        if (runCatching { contentRequester.requestFocus() }.isFailure) focusNav(section)
    }
    LaunchedEffect(Unit) {
        vm.homeEvents.collect {
            vm.dismissOptions()
            section = Section.BRIDGE
            focusContent = true
        }
    }
    LaunchedEffect(section, focusContent) {
        if (focusContent) {
            delay(320) // let the screen transition compose its focusable children
            if (runCatching { contentRequester.requestFocus() }.isFailure) focusNav(section)
            focusContent = false
        }
    }

    BackHandler {
        when {
            redAlert -> vm.setRedAlert(false)
            !navFocused -> {
                // Like tvOS: Back from the content first returns to the deck menu.
                vm.play(Sfx.BACK)
                focusNav(section)
            }
            section != Section.BRIDGE -> {
                vm.play(Sfx.BACK)
                section = Section.BRIDGE
                focusNav(Section.BRIDGE)
            }
            !isHomeMode() -> onExit()
            else -> navigate(Section.BRIDGE) // As home screen, Back on the bridge stays here.
        }
    }

    CompositionLocalProvider(
        LocalPalette provides palette,
        LocalLcarsSettings provides settings,
        LocalSfx provides vm::play,
        LocalStrings provides strings,
        LocalWeather provides weather,
        LocalRedAlert provides redAlert,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onPreviewKeyEvent { e ->
                    // The remote's search key (where the remote has one) opens the computer search.
                    if (e.type == KeyEventType.KeyDown && e.key == Key.Search) {
                        navigate(Section.SEARCH)
                        true
                    } else {
                        false
                    }
                },
        ) {
            LcarsFrame(
                title = section.title(strings),
                code = section.code,
                station = settings.shipName,
                expanded = navFocused,
                sidebar = { expanded ->
                    Section.entries.forEachIndexed { i, s ->
                        LcarsButton(
                            text = if (expanded) s.title(strings) else s.number,
                            color = if (s == Section.RED_ALERT) palette.alert else palette.pick(i + 1),
                            onClick = { navigate(s) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(navRequesters.getValue(s)),
                            shape = RectangleShape,
                            height = 36.dp,
                            textSize = if (expanded) 15 else 14,
                            selected = s == section && expanded,
                        )
                    }
                },
                onNavFocus = { navFocused = it },
                navEnter = { navRequesters.getValue(section) },
            ) {
                AnimatedContent(
                    targetState = section,
                    transitionSpec = {
                        if (settings.motion) {
                            (fadeIn(tween(260)) + slideInHorizontally(tween(300)) { it / 12 }) togetherWith fadeOut(tween(160))
                        } else {
                            fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                        }
                    },
                    label = "section",
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(contentRequester)
                        .focusGroup(),
                ) { target ->
                    when (target) {
                        Section.BRIDGE -> BridgeScreen(vm, onNavigate = { navigate(it) })
                        Section.SEARCH -> SearchScreen(vm, onNavigate = { navigate(it) })
                        Section.COMMUNICATIONS -> CommunicationsScreen(vm)
                        Section.HOLODECK -> HolodeckScreen(vm)
                        Section.ENGINEERING -> EngineeringScreen(vm)
                        Section.ASTROMETRICS -> AstrometricsScreen(vm)
                        Section.RED_ALERT -> RedAlertScreen(vm)
                        Section.SETTINGS -> SettingsScreen(vm)
                    }
                }
            }

            if (redAlert) RedAlertOverlay()

            AnimatedVisibility(
                visible = toast != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 72.dp),
            ) {
                LText(
                    toast.orEmpty(),
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.accent)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    size = 20.sp,
                    color = Color.Black,
                    bold = true,
                )
            }

            optionsApp?.let { AppOptionsSheet(vm, it) }
        }
    }
}

/**
 * The LCARS chrome: top elbow + header bars, a deck rail, and a lower elbow with the station name
 * and stardate. Like the tvOS / Google TV side menu, the rail folds to a slim strip of deck numbers
 * while you browse content and unfolds over the content when it has focus, so screens never reflow.
 * [navEnter] makes focus entering the rail land on the current deck.
 */
@Composable
private fun LcarsFrame(
    title: String,
    code: String,
    station: String,
    expanded: Boolean,
    sidebar: @Composable (expanded: Boolean) -> Unit,
    onNavFocus: (Boolean) -> Unit,
    navEnter: () -> FocusRequester,
    content: @Composable () -> Unit,
) {
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    val strings = LocalStrings.current
    val now = rememberNow(1000)
    val railWidth by animateDpAsState(
        if (expanded) SidebarWidth else RailWidth,
        tween(if (settings.motion) 220 else 0, easing = FastOutSlowInEasing),
        label = "rail",
    )

    // Boot animation: the bars sweep out from the elbows.
    val sweep = remember { Animatable(if (settings.motion) 0f else 1f) }
    LaunchedEffect(Unit) { sweep.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val sweepLayer = Modifier.graphicsLayer {
        scaleX = sweep.value
        transformOrigin = TransformOrigin(0f, 0.5f)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
        // ── Top: elbow + header ──
        Row(Modifier.fillMaxWidth().height(TopHeight)) {
            Box(
                Modifier
                    .width(railWidth + ElbowExtra)
                    .height(TopHeight)
                    .background(palette.primary, ElbowShape(railWidth, ArmHeight, 34.dp, 16.dp)),
            ) {
                LText(
                    if (expanded) "LCARS $code" else "LCARS",
                    Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 3.dp).width(railWidth - 14.dp),
                    size = 13.sp,
                    color = Color.Black,
                    bold = true,
                    align = TextAlign.End,
                )
            }
            Row(Modifier.weight(1f).height(TopHeight), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(sweepLayer.fillMaxWidth().height(ArmHeight)) {
                        Bar(palette.primary, Modifier.weight(3f).height(ArmHeight))
                        Spacer(Modifier.width(6.dp))
                        Bar(palette.secondary, Modifier.weight(1f).height(ArmHeight))
                        Spacer(Modifier.width(6.dp))
                        Bar(palette.tertiary, Modifier.width(48.dp).height(ArmHeight))
                        Spacer(Modifier.width(6.dp))
                    }
                    DataCascade(rows = 2, columns = 10, modifier = Modifier.padding(start = 14.dp, top = 5.dp), textSize = 11)
                }
                LText(title, Modifier.padding(horizontal = 10.dp), size = 34.sp, color = palette.primary, bold = true)
                Bar(palette.accent, Modifier.width(30.dp).height(ArmHeight), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
            }
        }

        // ── Middle: rail (overlays content when unfolded) + content ──
        Box(Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp)) {
            Box(Modifier.fillMaxSize().padding(start = RailWidth + ElbowExtra, end = 4.dp)) { content() }
            if (railWidth > RailWidth) {
                Box(
                    Modifier
                        .offset(x = railWidth)
                        .width(ElbowExtra + 60.dp)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(Color.Black, Color.Black.copy(alpha = 0.85f), Color.Transparent))),
                )
            }
            Column(
                Modifier
                    .width(railWidth)
                    .fillMaxHeight()
                    .background(Color.Black)
                    .onFocusChanged { onNavFocus(it.hasFocus) }
                    .focusProperties { enter = { navEnter() } }
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                sidebar(expanded)
                Bar(palette.tertiary, Modifier.fillMaxWidth().weight(1f))
            }
        }

        // ── Bottom: elbow + footer bar ──
        Row(Modifier.fillMaxWidth().height(BottomHeight)) {
            Box(
                Modifier
                    .width(railWidth + ElbowExtra)
                    .height(BottomHeight)
                    .background(palette.secondary, ElbowShape(railWidth, ArmHeight, 28.dp, 14.dp, flipVertical = true)),
            )
            Row(Modifier.weight(1f).height(BottomHeight), verticalAlignment = Alignment.Bottom) {
                Bar(palette.secondary, sweepLayer.width(70.dp).height(ArmHeight))
                Spacer(Modifier.width(10.dp))
                LText(station, size = 15.sp, color = palette.accent, bold = true)
                Spacer(Modifier.width(10.dp))
                Bar(palette.primary, sweepLayer.weight(1f).height(ArmHeight))
                Spacer(Modifier.width(6.dp))
                LText("${strings.stardateShort} ${Astro.formatStardate(Astro.stardate(now))}", size = 15.sp, color = palette.text)
                Spacer(Modifier.width(6.dp))
                Bar(palette.tertiary, Modifier.width(30.dp).height(ArmHeight), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
            }
        }
    }
}

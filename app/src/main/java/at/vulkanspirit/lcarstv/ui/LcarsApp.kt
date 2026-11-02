package at.vulkanspirit.lcarstv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.Astro
import at.vulkanspirit.lcarstv.ui.components.Bar
import at.vulkanspirit.lcarstv.ui.components.DataCascade
import at.vulkanspirit.lcarstv.ui.components.ElbowShape
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.screens.AstrometricsScreen
import at.vulkanspirit.lcarstv.ui.screens.BridgeScreen
import at.vulkanspirit.lcarstv.ui.screens.CommunicationsScreen
import at.vulkanspirit.lcarstv.ui.screens.ComputerCoreScreen
import at.vulkanspirit.lcarstv.ui.screens.EngineeringScreen
import at.vulkanspirit.lcarstv.ui.screens.HolodeckScreen
import at.vulkanspirit.lcarstv.ui.screens.RedAlertOverlay
import at.vulkanspirit.lcarstv.ui.screens.RedAlertScreen
import at.vulkanspirit.lcarstv.ui.screens.SettingsScreen
import at.vulkanspirit.lcarstv.ui.screens.rememberNow
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LcarsPalettes
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalSfx
import kotlinx.coroutines.delay

enum class Section(val title: String, val code: String, val spoken: String) {
    BRIDGE("MAIN BRIDGE", "01-1701", "Main bridge"),
    COMMUNICATIONS("COMMUNICATIONS", "02-4471", "Communications"),
    HOLODECK("HOLODECK", "03-0815", "Holodeck"),
    ENGINEERING("ENGINEERING", "04-2260", "Engineering"),
    ASTROMETRICS("ASTROMETRICS", "05-9912", "Astrometrics"),
    RED_ALERT("RED ALERT", "06-0001", "Tactical"),
    COMPUTER_CORE("COMPUTER CORE", "07-3341", "Computer core"),
    SETTINGS("SETTINGS", "08-5150", "Configuration"),
}

const val FOOTER_CREDIT = "MADE WITH AI — IDEAS BY MOHAMMAD J MALAYERI"

private val SidebarWidth = 150.dp
private val ElbowExtra = 34.dp
private val ArmHeight = 18.dp
private val TopHeight = 70.dp
private val BottomHeight = 52.dp

@Composable
fun LcarsApp(vm: LcarsViewModel, isHomeMode: () -> Boolean, onExit: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val redAlert by vm.redAlert.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()
    val basePalette = LcarsPalettes.forTheme(settings.theme)
    val palette = if (redAlert) LcarsPalettes.redAlert else basePalette

    var section by rememberSaveable { mutableStateOf(Section.BRIDGE) }
    var focusContent by remember { mutableStateOf(false) }
    val navRequesters = remember { Section.entries.associateWith { FocusRequester() } }
    val contentRequester = remember { FocusRequester() }

    fun focusNav(s: Section) = runCatching { navRequesters.getValue(s).requestFocus() }

    fun navigate(target: Section, moveFocusIntoContent: Boolean = true) {
        if (target != section) vm.say(target.spoken)
        section = target
        focusContent = moveFocusIntoContent
    }

    LaunchedEffect(Unit) {
        vm.play(Sfx.BOOT)
        vm.say("Welcome aboard the ${settings.shipName}")
        delay(150)
        focusNav(section)
    }
    LaunchedEffect(Unit) {
        vm.homeEvents.collect {
            section = Section.BRIDGE
            delay(100)
            focusNav(Section.BRIDGE)
        }
    }
    LaunchedEffect(section, focusContent) {
        if (focusContent) {
            delay(320) // let the screen transition compose its focusable children
            runCatching { contentRequester.requestFocus() }
            focusContent = false
        }
    }

    BackHandler {
        when {
            redAlert -> vm.setRedAlert(false)
            section != Section.BRIDGE -> {
                vm.play(Sfx.BACK)
                section = Section.BRIDGE
                focusNav(Section.BRIDGE)
            }
            !isHomeMode() -> onExit()
            else -> focusNav(Section.BRIDGE) // As home screen, Back on the bridge stays here.
        }
    }

    CompositionLocalProvider(
        LocalPalette provides palette,
        LocalLcarsSettings provides settings,
        LocalSfx provides vm::play,
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            LcarsFrame(
                title = section.title,
                code = section.code,
                sidebar = {
                    Section.entries.forEachIndexed { i, s ->
                        LcarsButton(
                            text = s.title,
                            color = if (s == Section.RED_ALERT) palette.alert else palette.pick(i + 1),
                            onClick = { navigate(s) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(navRequesters.getValue(s)),
                            shape = RectangleShape,
                            height = 38.dp,
                            textSize = 15,
                            selected = s == section,
                        )
                    }
                },
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
                        Section.COMMUNICATIONS -> CommunicationsScreen(vm)
                        Section.HOLODECK -> HolodeckScreen(vm)
                        Section.ENGINEERING -> EngineeringScreen(vm)
                        Section.ASTROMETRICS -> AstrometricsScreen(vm)
                        Section.RED_ALERT -> RedAlertScreen(vm)
                        Section.COMPUTER_CORE -> ComputerCoreScreen(vm)
                        Section.SETTINGS -> SettingsScreen(vm)
                    }
                }
            }

            if (redAlert) RedAlertOverlay()

            AnimatedVisibility(
                visible = toast != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 84.dp),
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
        }
    }
}

/**
 * The LCARS chrome: top elbow + header bars, a sidebar of section buttons, and a lower elbow
 * carrying the permanent footer credit. [navEnter] makes focus entering the sidebar land on the
 * current section rather than whichever button happens to be geometrically closest.
 */
@Composable
private fun LcarsFrame(
    title: String,
    code: String,
    sidebar: @Composable () -> Unit,
    navEnter: () -> FocusRequester,
    content: @Composable () -> Unit,
) {
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    val now = rememberNow(1000)

    // Boot animation: the bars sweep out from the elbows.
    val sweep = remember { Animatable(if (settings.motion) 0f else 1f) }
    LaunchedEffect(Unit) { sweep.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
    val sweepLayer = Modifier.graphicsLayer {
        scaleX = sweep.value
        transformOrigin = TransformOrigin(0f, 0.5f)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
        // ── Top: elbow + header ──
        Row(Modifier.fillMaxWidth().height(TopHeight)) {
            Box(
                Modifier
                    .width(SidebarWidth + ElbowExtra)
                    .height(TopHeight)
                    .background(palette.primary, ElbowShape(SidebarWidth, ArmHeight, 40.dp, 18.dp)),
            ) {
                LText(
                    "LCARS $code",
                    Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 4.dp).width(SidebarWidth - 20.dp),
                    size = 14.sp,
                    color = Color.Black,
                    bold = true,
                    align = TextAlign.End,
                )
            }
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth().height(TopHeight), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Row(sweepLayer.fillMaxWidth().height(ArmHeight)) {
                            Bar(palette.primary, Modifier.weight(3f).height(ArmHeight))
                            Spacer(Modifier.width(6.dp))
                            Bar(palette.secondary, Modifier.weight(1f).height(ArmHeight))
                            Spacer(Modifier.width(6.dp))
                            Bar(palette.tertiary, Modifier.width(48.dp).height(ArmHeight))
                            Spacer(Modifier.width(6.dp))
                        }
                        DataCascade(rows = 2, columns = 9, modifier = Modifier.padding(start = 16.dp, top = 6.dp))
                    }
                    LText(
                        title,
                        Modifier.padding(horizontal = 10.dp),
                        size = 40.sp,
                        color = palette.primary,
                        bold = true,
                    )
                    Bar(
                        palette.accent,
                        Modifier.width(30.dp).height(ArmHeight),
                        RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp),
                    )
                }
            }
        }

        // ── Middle: sidebar + content ──
        Row(Modifier.fillMaxWidth().weight(1f).padding(vertical = 5.dp)) {
            Column(
                Modifier
                    .width(SidebarWidth)
                    .focusProperties { enter = { navEnter() } }
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                sidebar()
                Bar(palette.tertiary, Modifier.fillMaxWidth().weight(1f))
            }
            Spacer(Modifier.width(ElbowExtra))
            Box(Modifier.weight(1f).padding(start = 4.dp, end = 4.dp)) { content() }
        }

        // ── Bottom: elbow + footer bar ──
        Row(Modifier.fillMaxWidth().height(BottomHeight)) {
            Box(
                Modifier
                    .width(SidebarWidth + ElbowExtra)
                    .height(BottomHeight)
                    .background(palette.secondary, ElbowShape(SidebarWidth, ArmHeight, 34.dp, 16.dp, flipVertical = true)),
            )
            Row(
                Modifier.weight(1f).height(BottomHeight),
                verticalAlignment = Alignment.Bottom,
            ) {
                Bar(palette.secondary, sweepLayer.width(70.dp).height(ArmHeight))
                Spacer(Modifier.width(10.dp))
                LText(FOOTER_CREDIT, Modifier.padding(bottom = 0.dp), size = 15.sp, color = palette.accent, bold = true)
                Spacer(Modifier.width(10.dp))
                Bar(palette.primary, sweepLayer.weight(1f).height(ArmHeight))
                Spacer(Modifier.width(6.dp))
                LText(
                    "SD ${Astro.formatStardate(Astro.stardate(now))}",
                    size = 15.sp,
                    color = palette.text,
                )
                Spacer(Modifier.width(6.dp))
                Bar(
                    palette.tertiary,
                    Modifier.width(30.dp).height(ArmHeight),
                    RoundedCornerShape(topEnd = 9.dp, bottomEnd = 9.dp),
                )
            }
        }
    }
}

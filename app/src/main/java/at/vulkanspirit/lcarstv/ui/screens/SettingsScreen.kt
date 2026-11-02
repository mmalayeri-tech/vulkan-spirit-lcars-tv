package at.vulkanspirit.lcarstv.ui.screens

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.BuildConfig
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.LcarsSettings
import at.vulkanspirit.lcarstv.data.LcarsThemeId
import at.vulkanspirit.lcarstv.data.Presets
import at.vulkanspirit.lcarstv.data.after
import at.vulkanspirit.lcarstv.ui.FOOTER_CREDIT
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.components.LcarsKeypad
import at.vulkanspirit.lcarstv.ui.components.LcarsRow
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette

private enum class SettingsPage { MAIN, SHIP_NAME, CLOCKS, HOME_MODE }

private fun onOff(b: Boolean) = if (b) "ON" else "OFF"

@Composable
fun SettingsScreen(vm: LcarsViewModel) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }
    if (page != SettingsPage.MAIN) {
        BackHandler {
            vm.play(Sfx.BACK)
            page = SettingsPage.MAIN
        }
    }
    when (page) {
        SettingsPage.MAIN -> MainSettings(vm) { page = it }
        SettingsPage.SHIP_NAME -> ShipNameEditor(vm) { page = SettingsPage.MAIN }
        SettingsPage.CLOCKS -> ClockPicker(vm) { page = SettingsPage.MAIN }
        SettingsPage.HOME_MODE -> HomeModePage(vm) { page = SettingsPage.MAIN }
    }
}

@Composable
private fun MainSettings(vm: LcarsViewModel, open: (SettingsPage) -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val context = LocalContext.current
    fun set(t: (LcarsSettings) -> LcarsSettings) = vm.updateSettings(t)

    val rows: List<Triple<String, String, () -> Unit>> = listOf(
        Triple("SHIP NAME", s.shipName) { open(SettingsPage.SHIP_NAME) },
        Triple("REGISTRY", s.registry) { set { it.copy(registry = Presets.registries.after(it.registry)) } },
        Triple("INTERFACE THEME", s.theme.label) { set { it.copy(theme = LcarsThemeId.entries.after(it.theme)) } },
        Triple("SOUND EFFECTS", onOff(s.soundsEnabled)) { set { it.copy(soundsEnabled = !it.soundsEnabled) } },
        Triple("NAVIGATION CHIRPS", onOff(s.navigationSounds)) { set { it.copy(navigationSounds = !it.navigationSounds) } },
        Triple("EFFECT VOLUME", "${s.volume}%") { set { it.copy(volume = Presets.volumes.after(it.volume).let { v -> v }) } },
        Triple("VOICE RESPONSES", if (s.voiceEnabled && !vm.voice.isAvailable) "ON (NO TTS ENGINE)" else onOff(s.voiceEnabled)) {
            val enabling = !s.voiceEnabled
            set { it.copy(voiceEnabled = enabling) }
            if (enabling) vm.voice.say("Voice responses enabled", force = true)
        },
        Triple("ANIMATIONS", onOff(s.animationsEnabled)) { set { it.copy(animationsEnabled = !it.animationsEnabled) } },
        Triple("REDUCED MOTION / NO FLASHING", onOff(s.reducedMotion)) { set { it.copy(reducedMotion = !it.reducedMotion) } },
        Triple("LARGE TEXT", onOff(s.largeText)) { set { it.copy(largeText = !it.largeText) } },
        Triple("CLOCK FORMAT", if (s.use24h) "24 HOUR" else "12 HOUR") { set { it.copy(use24h = !it.use24h) } },
        Triple("WEATHER SENSORS", onOff(s.weatherEnabled)) { set { it.copy(weatherEnabled = !it.weatherEnabled) } },
        Triple("WEATHER LOCATION", s.location.name) { set { it.copy(weatherLocation = (it.weatherLocation + 1) % Presets.locations.size) } },
        Triple("WORLD CLOCKS", "${s.worldClocks.size} SELECTED") { open(SettingsPage.CLOCKS) },
        Triple("HOME SCREEN MODE (OPTIONAL)", "CONFIGURE") { open(SettingsPage.HOME_MODE) },
        Triple("ANDROID SYSTEM SETTINGS", "OPEN") { vm.openSystemSettings(context, Settings.ACTION_SETTINGS) },
        Triple("ACCESSIBILITY SETTINGS", "OPEN") { vm.openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS) },
        Triple("RESTORE DEFAULTS", "RESET") { vm.resetSettings() },
    )

    Column(Modifier.fillMaxSize()) {
        Row {
            LText("CONFIGURATION", size = 18.sp, color = palette.secondary, bold = true, modifier = Modifier.weight(1f))
            LText("LCARS ${BuildConfig.VERSION_NAME}  ·  $FOOTER_CREDIT", size = 13.sp, color = palette.textDim)
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(rows) { i, (label, value, action) ->
                LcarsRow(label, value, palette.pick(i), action)
            }
        }
    }
}

@Composable
private fun ShipNameEditor(vm: LcarsViewModel, done: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    var draft by rememberSaveable { mutableStateOf(s.shipName) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(330.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LText("RENAME VESSEL", size = 18.sp, color = palette.secondary, bold = true)
            LText("U.S.S. ${draft}_", size = 26.sp, color = palette.primary, bold = true)
            LcarsKeypad(
                onKey = { draft = (draft + it).take(22) },
                onDelete = { draft = draft.dropLast(1) },
                onClear = { draft = "" },
                extraKey = "CONFIRM NAME" to {
                    if (draft.isNotBlank()) vm.updateSettings { it.copy(shipName = draft.trim()) }
                    done()
                },
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LText("OR CHOOSE A REGISTERED NAME", size = 18.sp, color = palette.secondary, bold = true)
            LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Presets.shipNames) { name ->
                    LcarsButton(name, palette.pick(name.length), {
                        vm.updateSettings { it.copy(shipName = name) }
                        done()
                    }, height = 40.dp, textSize = 16, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ClockPicker(vm: LcarsViewModel, done: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            LText("WORLD CLOCKS — SELECT UP TO 8", size = 18.sp, color = palette.secondary, bold = true, modifier = Modifier.weight(1f))
            LcarsButton("DONE", palette.primary, done, Modifier.width(100.dp), height = 34.dp, textSize = 15, textAlign = TextAlign.Center, sound = Sfx.BACK)
        }
        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(Presets.zones) { z ->
                val on = z.zoneId in s.worldClocks
                LcarsRow(z.label, if (on) "SHOWN" else "HIDDEN", if (on) palette.primary else palette.textDim, {
                    vm.updateSettings {
                        val list = if (on) it.worldClocks - z.zoneId else (it.worldClocks + z.zoneId).takeLast(8)
                        it.copy(worldClocks = list)
                    }
                })
            }
        }
    }
}

@Composable
private fun HomeModePage(vm: LcarsViewModel, done: () -> Unit) {
    val system by vm.system.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val context = LocalContext.current
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LText("HOME SCREEN MODE — OPTIONAL AND REVERSIBLE", size = 18.sp, color = palette.secondary, bold = true)
        LText(
            "LCARS ALWAYS WORKS AS A NORMAL APP FROM YOUR APPS ROW. THIS SWITCH ONLY ADVERTISES LCARS AS A " +
                "POSSIBLE HOME APP. IT NEVER DISABLES THE GOOGLE TV OR XIAOMI LAUNCHER. MANY GOOGLE TV " +
                "FIRMWARES IGNORE THIRD-PARTY HOME APPS, SO THE HOME BUTTON MAY STILL OPEN GOOGLE TV.",
            size = 15.sp,
            color = palette.text,
            maxLines = 5,
        )
        LcarsRow(
            "LCARS HOME ENTRY",
            if (system.homeAliasEnabled) "ENABLED" else "DISABLED",
            palette.primary,
            { vm.setHomeAlias(!system.homeAliasEnabled) },
        )
        LcarsRow("CURRENT DEFAULT HOME APP", system.defaultHome.uppercase(), palette.tertiary, { vm.refreshSystem() }, sound = null)
        LcarsRow(
            "OPEN DEFAULT APPS SETTINGS",
            "OPEN",
            palette.secondary,
            {
                vm.openSystemSettings(
                    context,
                    Settings.ACTION_HOME_SETTINGS,
                    Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS,
                )
            },
        )
        LText(
            if (system.isDefaultHome) "STATUS: LCARS IS THE DEFAULT HOME APP. TO UNDO, SWITCH THE ENTRY OFF ABOVE."
            else "STATUS: SYSTEM LAUNCHER REMAINS DEFAULT. SEE docs/HOME_LAUNCHER.md BEFORE TRYING ANYTHING MORE.",
            size = 14.sp,
            color = palette.textDim,
            maxLines = 2,
        )
        LcarsButton("BACK TO CONFIGURATION", palette.primary, done, Modifier.width(260.dp), height = 36.dp, textSize = 15, sound = Sfx.BACK)
    }
}

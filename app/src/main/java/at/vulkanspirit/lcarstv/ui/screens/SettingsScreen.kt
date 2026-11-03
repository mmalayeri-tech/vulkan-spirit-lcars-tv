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
import at.vulkanspirit.lcarstv.data.AppLanguage
import at.vulkanspirit.lcarstv.data.LcarsSettings
import at.vulkanspirit.lcarstv.data.LcarsThemeId
import at.vulkanspirit.lcarstv.data.Presets
import at.vulkanspirit.lcarstv.data.after
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.components.LcarsKeypad
import at.vulkanspirit.lcarstv.ui.components.LcarsRow
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings

private enum class SettingsPage { MAIN, SHIP_NAME, CLOCKS, HOME_MODE }

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
    val strings = LocalStrings.current
    val context = LocalContext.current
    fun set(t: (LcarsSettings) -> LcarsSettings) = vm.updateSettings(t)

    val rows: List<Triple<String, String, () -> Unit>> = listOf(
        Triple(strings.language, strings.languageName(s.language)) { set { it.copy(language = AppLanguage.entries.after(it.language)) } },
        Triple(strings.stationName, s.shipName) { open(SettingsPage.SHIP_NAME) },
        Triple(strings.designation, s.registry) { set { it.copy(registry = Presets.registries.after(it.registry)) } },
        Triple(strings.theme, s.theme.label) { set { it.copy(theme = LcarsThemeId.entries.after(it.theme)) } },
        Triple(strings.soundEffects, strings.onOff(s.soundsEnabled)) { set { it.copy(soundsEnabled = !it.soundsEnabled) } },
        Triple(strings.navigationChirps, strings.onOff(s.navigationSounds)) { set { it.copy(navigationSounds = !it.navigationSounds) } },
        Triple(strings.effectVolume, "${s.volume}%") { set { it.copy(volume = Presets.volumes.after(it.volume)) } },
        Triple(strings.voiceResponses, if (s.voiceEnabled && !vm.voice.isAvailable) strings.onNoTts else strings.onOff(s.voiceEnabled)) {
            val enabling = !s.voiceEnabled
            set { it.copy(voiceEnabled = enabling) }
            if (enabling) vm.voice.say(strings.voiceEnabled, force = true)
        },
        Triple(strings.animations, strings.onOff(s.animationsEnabled)) { set { it.copy(animationsEnabled = !it.animationsEnabled) } },
        Triple(strings.reducedMotion, strings.onOff(s.reducedMotion)) { set { it.copy(reducedMotion = !it.reducedMotion) } },
        Triple(strings.largeText, strings.onOff(s.largeText)) { set { it.copy(largeText = !it.largeText) } },
        Triple(strings.clockFormat, if (s.use24h) strings.hours24 else strings.hours12) { set { it.copy(use24h = !it.use24h) } },
        Triple(strings.weatherSensors, strings.onOff(s.weatherEnabled)) { set { it.copy(weatherEnabled = !it.weatherEnabled) } },
        Triple(strings.weatherLocation, strings.city(s.location.name)) { set { it.copy(weatherLocation = (it.weatherLocation + 1) % Presets.locations.size) } },
        Triple(strings.worldClocks, strings.selectedCount(s.worldClocks.size)) { open(SettingsPage.CLOCKS) },
        Triple(strings.homeScreenMode, strings.configure) { open(SettingsPage.HOME_MODE) },
        Triple(strings.androidSystemSettings, strings.open) { vm.openSystemSettings(context, Settings.ACTION_SETTINGS) },
        Triple(strings.accessibilitySettings, strings.open) { vm.openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS) },
        Triple(strings.restoreDefaults, strings.reset) { vm.resetSettings() },
    )

    Column(Modifier.fillMaxSize()) {
        Row {
            LText(strings.configuration, size = 18.sp, color = palette.secondary, bold = true, modifier = Modifier.weight(1f))
            LText("LCARS ${BuildConfig.VERSION_NAME}", size = 13.sp, color = palette.textDim)
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
    val strings = LocalStrings.current
    var draft by rememberSaveable { mutableStateOf(s.shipName) }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LText(strings.renameStation, size = 18.sp, color = palette.secondary, bold = true)
            LText("${draft}_", size = 22.sp, color = palette.primary, bold = true, maxLines = 2)
            LcarsKeypad(
                onKey = { draft = (draft + it).take(32) },
                onDelete = { draft = draft.dropLast(1) },
                onClear = { draft = "" },
                extraKey = strings.confirmName to {
                    if (draft.isNotBlank()) vm.updateSettings { it.copy(shipName = draft.trim()) }
                    done()
                },
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LText(strings.chooseRegistered, size = 18.sp, color = palette.secondary, bold = true)
            LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Presets.stationNames) { name ->
                    LcarsButton(name, palette.pick(name.length), {
                        vm.updateSettings { it.copy(shipName = name) }
                        done()
                    }, height = 40.dp, textSize = 15, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ClockPicker(vm: LcarsViewModel, done: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            LText(strings.worldClocksPick, size = 18.sp, color = palette.secondary, bold = true, modifier = Modifier.weight(1f))
            LcarsButton(strings.done, palette.primary, done, Modifier.width(100.dp), height = 34.dp, textSize = 15, textAlign = TextAlign.Center, sound = Sfx.BACK)
        }
        LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(Presets.zones) { z ->
                val on = z.zoneId in s.worldClocks
                LcarsRow(strings.city(z.label), if (on) strings.shown else strings.hidden, if (on) palette.primary else palette.textDim, {
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
    val strings = LocalStrings.current
    val context = LocalContext.current
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LText(strings.homeModeTitle, size = 18.sp, color = palette.secondary, bold = true)
        LText(
            strings.homeModeExplanation,
            size = 15.sp,
            color = palette.text,
            maxLines = 5,
        )
        LcarsRow(
            strings.homeEntry,
            if (system.homeAliasEnabled) strings.enabled else strings.disabled,
            palette.primary,
            { vm.setHomeAlias(!system.homeAliasEnabled) },
        )
        LcarsRow(strings.currentHome, if (system.defaultHome == "SYSTEM CHOOSER") strings.systemChooser else system.defaultHome.uppercase(), palette.tertiary, { vm.refreshSystem() }, sound = null)
        LcarsRow(
            strings.openDefaultApps,
            strings.open,
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
            if (system.isDefaultHome) strings.homeStatusDefault else strings.homeStatusSystem,
            size = 14.sp,
            color = palette.textDim,
            maxLines = 2,
        )
        LcarsButton(strings.backToConfiguration, palette.primary, done, Modifier.width(260.dp), height = 36.dp, textSize = 15, sound = Sfx.BACK)
    }
}

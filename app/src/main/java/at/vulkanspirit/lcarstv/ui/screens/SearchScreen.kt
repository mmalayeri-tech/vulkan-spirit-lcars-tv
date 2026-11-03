package at.vulkanspirit.lcarstv.ui.screens

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.AppSearch
import at.vulkanspirit.lcarstv.data.StationCommand
import at.vulkanspirit.lcarstv.i18n.De
import at.vulkanspirit.lcarstv.i18n.En
import at.vulkanspirit.lcarstv.ui.Section
import at.vulkanspirit.lcarstv.ui.components.AppCard
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.components.LcarsKeypad
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
import kotlinx.coroutines.delay

/** Library quick-access titles; selecting one searches for it. */
private val STAR_TREK_TITLES = listOf(
    "STAR TREK", "STRANGE NEW WORLDS", "THE NEXT GENERATION", "DEEP SPACE NINE", "VOYAGER",
    "DISCOVERY", "PICARD", "LOWER DECKS", "PRODIGY", "THE ORIGINAL SERIES", "ENTERPRISE", "STARFLEET ACADEMY",
)

private const val MAX_QUERY = 40

/**
 * "Computer, search for…": one query searches installed programs, offers to search content for
 * it inside every app that supports search (and Google TV / Play Store when present), and matches
 * station commands such as red alert or Wi-Fi settings, in English or German.
 */
@Composable
fun SearchScreen(vm: LcarsViewModel, onNavigate: (Section) -> Unit) {
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val apps by vm.apps.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val globalSearch by vm.globalSearch.collectAsStateWithLifecycle()
    val playStore by vm.playStore.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val resultsFocus = remember { FocusRequester() }
    var jumpToResults by remember { mutableStateOf(false) }
    LaunchedEffect(jumpToResults) {
        if (jumpToResults) {
            delay(150) // the quick-access buttons are replaced by results; move focus onto them
            runCatching { resultsFocus.requestFocus() }
            jumpToResults = false
        }
    }

    val results = remember(apps, q) {
        if (q.isEmpty()) {
            emptyList()
        } else {
            val byName = AppSearch.search(apps, q, { it.label }, { it.packageName })
            // A category name ("games", "musik") also lists that category's apps.
            val groups = AppGroup.entries.filter { g ->
                listOf(En.groupRow(g), En.groupShort(g), De.groupRow(g), De.groupShort(g)).any { it.lowercase().startsWith(q.lowercase()) }
            }
            (byName + apps.filter { it.group in groups }).distinctBy { it.packageName }
        }
    }
    val searchable = remember(apps) { apps.filter { it.searchable } }
    val decks = remember(q) {
        if (q.length < 2) emptyList()
        else Section.entries.filter { s -> listOf(s.title(En), s.title(De)).any { it.lowercase().contains(q.lowercase()) } }
    }
    val commands = remember(q) { StationCommand.match(q) }

    // Speak the result count once typing pauses (only when voice responses are on).
    LaunchedEffect(q) {
        if (q.isNotEmpty()) {
            delay(1400)
            vm.say(strings.voiceMatches(results.size))
        }
    }

    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val heard = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (r.resultCode == Activity.RESULT_OK && !heard.isNullOrBlank()) query = heard.uppercase(strings.locale).take(MAX_QUERY)
    }
    val speechIntent = remember(strings) {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, strings.locale.toLanguageTag())
            .putExtra(RecognizerIntent.EXTRA_PROMPT, strings.searchPrompt)
    }
    val canListen = remember(speechIntent) {
        context.packageManager.resolveActivity(speechIntent, PackageManager.MATCH_DEFAULT_ONLY) != null
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LText(strings.searchPrompt, size = 17.sp, color = palette.secondary, bold = true)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(palette.panel)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                LText(
                    if (query.isEmpty()) "${strings.awaitingInput}_" else "${query}_",
                    size = 21.sp,
                    color = if (query.isEmpty()) palette.textDim else palette.text,
                    bold = true,
                )
            }
            LcarsKeypad(
                onKey = { query = (query + it).take(MAX_QUERY) },
                onDelete = { query = query.dropLast(1) },
                onClear = { query = "" },
                extraKey = strings.rescan to { vm.refreshApps(); vm.showToast(strings.rescanned) },
                trailing = {
                    if (canListen) {
                        LcarsButton(
                            strings.voiceInput, palette.highlight,
                            { runCatching { speech.launch(speechIntent) }.onFailure { vm.play(Sfx.ERROR) } },
                            Modifier.width(110.dp), height = 34.dp, textSize = 14, textAlign = TextAlign.Center,
                        )
                    }
                },
            )
        }

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
            if (q.isEmpty()) {
                item(key = "quick") { SectionTitle(strings.quickAccess) }
                items(STAR_TREK_TITLES.chunked(3), key = { it.first() }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { i, title ->
                            LcarsButton(
                                title, palette.pick(i + title.length),
                                {
                                    query = if (title == "STAR TREK") title else "STAR TREK $title"
                                    jumpToResults = true
                                },
                                Modifier.weight(1f), height = 38.dp, textSize = 15, textAlign = TextAlign.Center, sound = Sfx.TOGGLE,
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                val recents = settings.recents.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
                if (recents.isNotEmpty()) {
                    item(key = "recent-title") { SectionTitle(strings.rowRecent) }
                    item(key = "recent") { CardRow(vm, recents, settings.favorites) }
                }
                return@LazyColumn
            }

            item(key = "programs-title") { SectionTitle(strings.resultsPrograms(results.size)) }
            item(key = "programs") {
                if (results.isEmpty()) {
                    LText(strings.noMatch(q), Modifier.padding(start = 8.dp), size = 16.sp, color = palette.textDim)
                } else {
                    CardRow(vm, results.take(24), settings.favorites)
                }
            }

            item(key = "content-title") { SectionTitle(strings.searchContentFor(q)) }
            item(key = "content") {
                val targets = buildList<Pair<String, () -> Unit>> {
                    if (globalSearch) add(strings.everywhere to { vm.searchEverywhere(context, q) })
                    searchable.forEach { app -> add(app.label.uppercase() to { vm.searchIn(context, app, q) }) }
                    if (playStore) add(strings.playStore to { vm.searchPlayStore(context, q) })
                }.distinctBy { it.first }
                if (targets.isEmpty()) {
                    LText(strings.noContentSearch, Modifier.padding(start = 8.dp), size = 15.sp, color = palette.textDim)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)) {
                        items(targets.size) { i ->
                            val (label, action) = targets[i]
                            LcarsButton(
                                label, palette.pick(i), action,
                                Modifier.width(170.dp).then(if (i == 0) Modifier.focusRequester(resultsFocus) else Modifier),
                                height = 40.dp, textSize = 15, textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            if (decks.isNotEmpty() || commands.isNotEmpty()) {
                item(key = "commands-title") { SectionTitle(strings.commands) }
                item(key = "commands") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        decks.forEach { s ->
                            LcarsButton(strings.openDeck(s.title(strings)), palette.tertiary, { onNavigate(s) }, Modifier.fillMaxWidth(0.8f), height = 38.dp, textSize = 15, textAlign = TextAlign.Start)
                        }
                        commands.forEach { c ->
                            LcarsButton(
                                strings.command(c),
                                if (c == StationCommand.RED_ALERT) palette.alert else palette.secondary,
                                {
                                    when (val action = c.settingsAction) {
                                        null -> { vm.setRedAlert(true); onNavigate(Section.RED_ALERT) }
                                        else -> vm.openSystemSettings(context, action)
                                    }
                                },
                                Modifier.fillMaxWidth(0.8f), height = 38.dp, textSize = 15, textAlign = TextAlign.Start,
                                sound = if (c == StationCommand.RED_ALERT) null else Sfx.SELECT,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    val palette = LocalPalette.current
    Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(width = 18.dp, height = 12.dp).clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)).background(palette.secondary))
        Spacer(Modifier.width(6.dp))
        LText(text, size = 15.sp, color = palette.secondary, bold = true)
    }
}

@Composable
private fun CardRow(vm: LcarsViewModel, apps: List<at.vulkanspirit.lcarstv.data.InstalledApp>, favorites: List<String>) {
    val context = LocalContext.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
        items(apps, key = { it.packageName }) { app ->
            AppCard(
                app = app,
                isFavorite = app.packageName in favorites,
                onClick = { vm.launch(context, app) },
                onLongClick = { vm.showOptions(app) },
                width = 136.dp,
            )
        }
    }
}

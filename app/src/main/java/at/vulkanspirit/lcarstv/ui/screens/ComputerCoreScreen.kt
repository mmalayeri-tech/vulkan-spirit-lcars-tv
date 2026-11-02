package at.vulkanspirit.lcarstv.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.AppSearch
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.components.LcarsKeypad
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette

private enum class CoreTab { SEARCH, FAVORITES }

@Composable
fun ComputerCoreScreen(vm: LcarsViewModel) {
    val palette = LocalPalette.current
    val apps by vm.apps.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableStateOf(CoreTab.SEARCH) }
    val results = remember(apps, query) { AppSearch.search(apps, query, { it.label }, { it.packageName }) }
    val favorites = remember(apps, settings.favorites) {
        settings.favorites.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    }

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Column(Modifier.width(330.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LText("QUERY THE COMPUTER", size = 18.sp, color = palette.secondary, bold = true)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(palette.panel)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                LText(if (query.isEmpty()) "AWAITING INPUT_" else "${query}_", size = 22.sp, color = if (query.isEmpty()) palette.textDim else palette.text, bold = true)
            }
            LcarsKeypad(
                onKey = { query = (query + it).take(24); tab = CoreTab.SEARCH },
                onDelete = { query = query.dropLast(1) },
                onClear = { query = "" },
                extraKey = "RESCAN PROGRAMS" to { vm.refreshApps(); vm.showToast("PROGRAM DATABASE RESCANNED") },
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                LcarsButton("RESULTS (${results.size})", palette.primary, { tab = CoreTab.SEARCH }, Modifier.width(150.dp), height = 34.dp, textSize = 15, selected = tab == CoreTab.SEARCH, sound = Sfx.TOGGLE)
                LcarsButton("FAVORITES (${favorites.size})", palette.tertiary, { tab = CoreTab.FAVORITES }, Modifier.width(170.dp), height = 34.dp, textSize = 15, selected = tab == CoreTab.FAVORITES, sound = Sfx.TOGGLE)
            }
            when (tab) {
                CoreTab.SEARCH -> AppList(
                    items = results,
                    empty = if (query.isEmpty()) "NO PROGRAMS FOUND" else "NO MATCH FOR \"$query\"",
                ) { app ->
                    AppRowActions(vm, app, isFavorite = app.packageName in settings.favorites, showOrder = false)
                }
                CoreTab.FAVORITES -> AppList(
                    items = favorites,
                    empty = "NO FAVORITES. SELECT ☆ PIN ON ANY SEARCH RESULT.",
                ) { app ->
                    AppRowActions(vm, app, isFavorite = true, showOrder = true)
                }
            }
        }
    }
}

@Composable
private fun AppList(items: List<InstalledApp>, empty: String, row: @Composable (InstalledApp) -> Unit) {
    val palette = LocalPalette.current
    if (items.isEmpty()) {
        LText(empty, size = 18.sp, color = palette.textDim, modifier = Modifier.padding(top = 20.dp))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items, key = { it.packageName }) { row(it) }
    }
}

@Composable
private fun AppRowActions(vm: LcarsViewModel, app: InstalledApp, isFavorite: Boolean, showOrder: Boolean) {
    val palette = LocalPalette.current
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        app.icon?.let { Image(it, null, Modifier.size(32.dp)) } ?: Spacer(Modifier.size(32.dp))
        LcarsButton(
            text = app.label.uppercase(),
            color = palette.primary,
            onClick = { vm.launch(context, app) },
            modifier = Modifier.weight(1f),
            height = 36.dp,
            textSize = 16,
            textAlign = TextAlign.Start,
            sound = null,
        )
        if (showOrder) {
            LcarsButton("UP", palette.tertiary, { vm.moveFavorite(app.packageName, up = true) }, Modifier.width(54.dp), height = 36.dp, textSize = 14, textAlign = TextAlign.Center, sound = Sfx.TOGGLE)
            LcarsButton("DOWN", palette.tertiary, { vm.moveFavorite(app.packageName, up = false) }, Modifier.width(66.dp), height = 36.dp, textSize = 14, textAlign = TextAlign.Center, sound = Sfx.TOGGLE)
        }
        LcarsButton(
            text = if (isFavorite) "★ UNPIN" else "☆ PIN",
            color = if (isFavorite) palette.highlight else palette.secondary,
            onClick = { vm.toggleFavorite(app.packageName) },
            modifier = Modifier.width(96.dp),
            height = 36.dp,
            textSize = 14,
            textAlign = TextAlign.Center,
            sound = null,
        )
    }
}

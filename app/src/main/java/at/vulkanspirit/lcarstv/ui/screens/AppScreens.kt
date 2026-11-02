package at.vulkanspirit.lcarstv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.AppCategorizer
import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.ui.components.AppTile
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

/** Current time, re-read every [intervalMs]. */
@Composable
fun rememberNow(intervalMs: Long): ZonedDateTime {
    val now by produceState(ZonedDateTime.now()) {
        while (true) {
            delay(intervalMs - System.currentTimeMillis() % intervalMs)
            value = ZonedDateTime.now()
        }
    }
    return now
}

@Composable
fun AppGrid(
    vm: LcarsViewModel,
    apps: List<InstalledApp>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    columns: Int = 4,
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val loading by vm.appsLoading.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    if (apps.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LText(
                if (loading) "SCANNING SUBSPACE FOR PROGRAMS…" else emptyMessage,
                size = 22.sp,
                color = palette.textDim,
                align = TextAlign.Center,
                maxLines = 3,
            )
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        itemsIndexed(apps, key = { _, a -> a.packageName }) { i, app ->
            AppTile(
                app = app,
                isFavorite = app.packageName in settings.favorites,
                colorIndex = i,
                onLaunch = { vm.launch(context, app) },
                onToggleFavorite = {
                    vm.toggleFavorite(app.packageName)
                    vm.showToast(
                        if (app.packageName in settings.favorites) "${app.label.uppercase()} REMOVED FROM FAVORITES"
                        else "${app.label.uppercase()} ADDED TO FAVORITES",
                    )
                },
            )
        }
    }
}

@Composable
fun CommunicationsScreen(vm: LcarsViewModel) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val comms = remember(apps) { apps.filter { AppCategorizer.isCommunicationsDeck(it.group) } }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LText("SUBSPACE CHANNELS OPEN: ${comms.size}", size = 20.sp, color = palette.secondary, bold = true)
            Spacer(Modifier.weight(1f))
            LText("HOLD OK ON A CHANNEL TO PIN IT AS A FAVORITE", size = 14.sp, color = palette.textDim)
        }
        AppGrid(vm, comms, "NO STREAMING OR COMMUNICATION APPS DETECTED.\nSEE HOLODECK FOR ALL PROGRAMS.")
    }
}

private enum class HoloFilter(val label: String, val groups: Set<AppGroup>?) {
    ALL("ALL", null),
    VIDEO("VIDEO", setOf(AppGroup.VIDEO)),
    AUDIO("AUDIO", setOf(AppGroup.MUSIC)),
    GAMES("GAMES", setOf(AppGroup.GAMES)),
    COMMS("COMMS", setOf(AppGroup.COMMUNICATION)),
    OTHER("OTHER", setOf(AppGroup.OTHER, AppGroup.SYSTEM)),
}

@Composable
fun HolodeckScreen(vm: LcarsViewModel) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    var filter by rememberSaveable { mutableStateOf(HoloFilter.ALL) }
    val shown = remember(apps, filter) { apps.filter { app -> filter.groups?.contains(app.group) ?: true } }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HoloFilter.entries.forEachIndexed { i, f ->
                LcarsButton(
                    text = f.label,
                    color = palette.pick(i),
                    onClick = { filter = f },
                    modifier = Modifier.width(86.dp),
                    height = 32.dp,
                    textSize = 14,
                    textAlign = TextAlign.Center,
                    selected = f == filter,
                    sound = Sfx.TOGGLE,
                )
            }
            Spacer(Modifier.weight(1f))
            LText("PROGRAMS: ${shown.size}", size = 18.sp, color = palette.primary, bold = true)
        }
        AppGrid(vm, shown, "NO HOLODECK PROGRAMS IN THIS CATEGORY.")
    }
}

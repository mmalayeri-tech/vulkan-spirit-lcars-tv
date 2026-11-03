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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.AppCategorizer
import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.ui.components.AppCard
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
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
    columns: Int = 5,
) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val loading by vm.appsLoading.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    if (apps.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LText(
                if (loading) strings.scanning else emptyMessage,
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
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppCard(
                app = app,
                isFavorite = app.packageName in settings.favorites,
                onClick = { vm.launch(context, app) },
                onLongClick = { vm.showOptions(app) },
                width = Dp.Unspecified,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun CommunicationsScreen(vm: LcarsViewModel) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val comms = remember(apps) { apps.filter { AppCategorizer.isCommunicationsDeck(it.group) } }
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LText(strings.channelsOpen(comms.size), size = 20.sp, color = palette.secondary, bold = true)
            Spacer(Modifier.weight(1f))
            LText(strings.holdOkHint, size = 14.sp, color = palette.textDim)
        }
        AppGrid(vm, comms, strings.noCommsApps)
    }
}

private val HOLO_FILTERS: List<Set<AppGroup>?> = listOf(
    null,
    setOf(AppGroup.VIDEO),
    setOf(AppGroup.MUSIC),
    setOf(AppGroup.GAMES),
    setOf(AppGroup.COMMUNICATION),
    setOf(AppGroup.OTHER, AppGroup.SYSTEM),
)

@Composable
fun HolodeckScreen(vm: LcarsViewModel) {
    val apps by vm.apps.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    var filterIndex by rememberSaveable { mutableIntStateOf(0) }
    val filter = HOLO_FILTERS[filterIndex]
    val shown = remember(apps, filter) { apps.filter { app -> filter?.contains(app.group) ?: true } }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HOLO_FILTERS.forEachIndexed { i, f ->
                LcarsButton(
                    text = f?.first()?.let(strings::groupShort) ?: strings.filterAll,
                    color = palette.pick(i),
                    onClick = { filterIndex = i },
                    modifier = Modifier.width(92.dp),
                    height = 32.dp,
                    textSize = 14,
                    textAlign = TextAlign.Center,
                    selected = i == filterIndex,
                    sound = Sfx.TOGGLE,
                )
            }
            Spacer(Modifier.weight(1f))
            LText(strings.programsCount(shown.size), size = 18.sp, color = palette.primary, bold = true)
        }
        AppGrid(vm, shown, strings.noHoloApps)
    }
}

package at.vulkanspirit.lcarstv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings
import kotlinx.coroutines.delay

/**
 * The long-press (hold OK) sheet for an app, like the tvOS context menu: open, pin/unpin,
 * reorder pinned apps, forget from "recently used", or jump to Android's App info.
 * A Dialog keeps D-pad focus inside the sheet; Back closes it.
 */
@Composable
fun AppOptionsSheet(vm: LcarsViewModel, app: InstalledApp) {
    val palette = LocalPalette.current
    val strings = LocalStrings.current
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val isFavorite = app.packageName in settings.favorites
    val favIndex = settings.favorites.indexOf(app.packageName)
    val first = remember { FocusRequester() }
    LaunchedEffect(app.packageName) {
        delay(60)
        runCatching { first.requestFocus() }
    }
    fun close() = vm.dismissOptions()

    Dialog(onDismissRequest = ::close, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Row(
            Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(topStart = 36.dp, bottomStart = 36.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                .background(Color.Black)
                .padding(10.dp),
        ) {
            // LCARS cap carrying the app artwork.
            Column(
                Modifier
                    .width(220.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp))
                    .background(app.accent?.let { Color(it) } ?: palette.primary)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(10.dp)).background(palette.panel), contentAlignment = Alignment.Center) {
                    when {
                        app.banner != null -> Image(app.banner, null, Modifier.fillMaxWidth().fillMaxHeight(), contentScale = ContentScale.Crop)
                        app.icon != null -> Image(app.icon, null, Modifier.size(64.dp))
                    }
                }
                LText(app.label.uppercase(), size = 22.sp, color = Color.Black, bold = true, maxLines = 2)
                LText(strings.groupRow(app.group), size = 14.sp, color = Color.Black)
                if (!app.isTvApp) LText(strings.phoneApp, size = 12.sp, color = Color.Black, maxLines = 2)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val buttonHeight = 38.dp
                LcarsButton(strings.actOpen, palette.primary, { close(); vm.launch(context, app) }, Modifier.fillMaxWidth().focusRequester(first), height = buttonHeight, textSize = 16, sound = null)
                LcarsButton(
                    if (isFavorite) strings.actUnpin else strings.actPin,
                    palette.highlight,
                    { vm.toggleFavorite(app); close() },
                    Modifier.fillMaxWidth(), height = buttonHeight, textSize = 16, sound = null,
                )
                if (isFavorite && settings.favorites.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LcarsButton(strings.actMoveLeft, palette.tertiary, { vm.moveFavorite(app.packageName, up = true) }, Modifier.weight(1f).height(buttonHeight), height = buttonHeight, textSize = 15, textAlign = TextAlign.Center, sound = if (favIndex > 0) Sfx.TOGGLE else Sfx.ERROR)
                        LcarsButton(strings.actMoveRight, palette.tertiary, { vm.moveFavorite(app.packageName, up = false) }, Modifier.weight(1f).height(buttonHeight), height = buttonHeight, textSize = 15, textAlign = TextAlign.Center, sound = if (favIndex < settings.favorites.lastIndex) Sfx.TOGGLE else Sfx.ERROR)
                    }
                }
                if (app.packageName in settings.recents) {
                    LcarsButton(strings.actForget, palette.secondary, { vm.forgetRecent(app.packageName); close() }, Modifier.fillMaxWidth(), height = buttonHeight, textSize = 16, sound = Sfx.BACK)
                }
                LcarsButton(strings.actAppInfo, palette.accent, { close(); vm.openAppInfo(context, app) }, Modifier.fillMaxWidth(), height = buttonHeight, textSize = 16)
                LcarsButton(strings.actCancel, palette.textDim, ::close, Modifier.fillMaxWidth(), height = buttonHeight, textSize = 16, sound = Sfx.BACK)
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

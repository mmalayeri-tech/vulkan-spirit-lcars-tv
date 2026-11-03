package at.vulkanspirit.lcarstv.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalLcarsSettings
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import at.vulkanspirit.lcarstv.ui.theme.LocalStrings

// Pulse period is 1.6 s (well under the 3 flashes/second photosensitivity guideline).
private const val PULSE_MS = 800

@Composable
fun RedAlertScreen(vm: LcarsViewModel) {
    val active by vm.redAlert.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val settings = LocalLcarsSettings.current
    val strings = LocalStrings.current
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LText(strings.tacticalCondition, size = 20.sp, color = palette.secondary, bold = true)
            LText(
                if (active) strings.deckRedAlert else strings.conditionGreen,
                size = 48.sp,
                color = if (active) palette.alert else Color(0xFF66CC66),
                bold = true,
            )
            LText(
                if (active) strings.alertActiveHint else strings.alertIdleHint,
                size = 16.sp,
                color = palette.textDim,
                maxLines = 3,
            )
            LcarsButton(
                text = if (active) strings.standDown else strings.engageRedAlert,
                color = if (active) Color(0xFF66AA66) else palette.alert,
                onClick = { vm.setRedAlert(!active) },
                modifier = Modifier.width(300.dp),
                height = 64.dp,
                textSize = 26,
                textAlign = TextAlign.Center,
                sound = null,
            )
            LText(
                when {
                    !settings.soundsEnabled -> strings.klaxonMuted
                    else -> strings.klaxonInfo
                },
                size = 13.sp,
                color = palette.textDim,
            )
            LText(
                if (settings.motion) strings.photosensitiveHint else strings.reducedMotionActive,
                size = 13.sp,
                color = palette.textDim,
                maxLines = 2,
            )
            LcarsButton(strings.testAlertTone, palette.secondary, { vm.play(Sfx.ERROR) }, Modifier.width(200.dp), height = 34.dp, textSize = 14, sound = null)
        }
        AlertBars(active, Modifier.weight(1f).fillMaxSize())
    }
}

/** The classic stack of horizontal bars that converge on the centre during an alert. */
@Composable
private fun AlertBars(active: Boolean, modifier: Modifier) {
    val palette = LocalPalette.current
    val motion = LocalLcarsSettings.current.motion
    val phase by rememberInfiniteTransition(label = "bars").animateFloat(
        0f, 1f, infiniteRepeatable(tween(PULSE_MS * 2, easing = LinearEasing)), label = "phase",
    )
    Canvas(modifier) {
        val rows = 9
        val gap = size.height * 0.02f
        val barH = (size.height - gap * (rows - 1)) / rows
        for (i in 0 until rows) {
            val fromCenter = kotlin.math.abs(i - rows / 2).toFloat() / (rows / 2)
            val p = if (active && motion) ((phase + fromCenter * 0.5f) % 1f) else 0.5f
            val widthFactor = if (active) 0.35f + 0.65f * (1f - fromCenter) * (0.6f + 0.4f * kotlin.math.sin(p * 2 * Math.PI).toFloat()) else 0.25f + 0.5f * (1f - fromCenter)
            val w = size.width * widthFactor
            val color = if (active) palette.alert.copy(alpha = 0.55f + 0.45f * (1f - fromCenter)) else palette.textDim.copy(alpha = 0.35f)
            drawRoundRect(
                color,
                topLeft = Offset((size.width - w) / 2, i * (barH + gap)),
                size = Size(w, barH),
                cornerRadius = CornerRadius(barH / 2),
            )
        }
    }
}

/**
 * Global overlay while red alert is active: a pulsing red frame and banner over every deck.
 * It draws only; it holds no focus, so the remote keeps working underneath.
 */
@Composable
fun RedAlertOverlay() {
    val motion = LocalLcarsSettings.current.motion
    val strings = LocalStrings.current
    val pulse by rememberInfiniteTransition(label = "alert").animateFloat(
        0.25f, 1f, infiniteRepeatable(tween(PULSE_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse",
    )
    val a = if (motion) pulse else 0.8f
    val red = Color(0xFFFF2020)
    Box(Modifier.fillMaxSize().border(6.dp, red.copy(alpha = a))) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .border(3.dp, red.copy(alpha = a))
                .padding(horizontal = 26.dp, vertical = 2.dp),
        ) {
            LText(strings.deckRedAlert, size = 26.sp, color = red.copy(alpha = a), bold = true, letterSpacing = 6.sp)
        }
        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(4.dp).border(2.dp, red.copy(alpha = a)))
    }
}

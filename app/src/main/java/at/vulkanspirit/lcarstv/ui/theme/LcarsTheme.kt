package at.vulkanspirit.lcarstv.ui.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import at.vulkanspirit.lcarstv.R
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.data.LcarsSettings
import at.vulkanspirit.lcarstv.data.LcarsThemeId

data class LcarsPalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val accent: Color,
    val highlight: Color,
    val alert: Color,
    val text: Color,
    val textDim: Color,
    val panel: Color,
    val focus: Color = Color.White,
    val background: Color = Color.Black,
) {
    val cycle: List<Color> get() = listOf(primary, secondary, tertiary, accent, highlight)
    fun pick(index: Int): Color = cycle[index.mod(cycle.size)]
}

object LcarsPalettes {
    val enterpriseD = LcarsPalette(
        primary = Color(0xFFFF9900), secondary = Color(0xFFCC99CC), tertiary = Color(0xFF9999FF),
        accent = Color(0xFFFFCC99), highlight = Color(0xFFFFAA33), alert = Color(0xFFCC4444),
        text = Color(0xFFFFCC99), textDim = Color(0xFFB38A5E), panel = Color(0xFF15100A),
    )
    val voyager = LcarsPalette(
        primary = Color(0xFF9999FF), secondary = Color(0xFFCC88CC), tertiary = Color(0xFFFFAA66),
        accent = Color(0xFFCCDDFF), highlight = Color(0xFF6677CC), alert = Color(0xFFDD4444),
        text = Color(0xFFCCDDFF), textDim = Color(0xFF8090B8), panel = Color(0xFF0C0D18),
    )
    val deepSpaceNine = LcarsPalette(
        primary = Color(0xFFCC7744), secondary = Color(0xFFAA88AA), tertiary = Color(0xFF7788AA),
        accent = Color(0xFFDDAA77), highlight = Color(0xFFBB5533), alert = Color(0xFFCC3333),
        text = Color(0xFFEEC9A0), textDim = Color(0xFFA08060), panel = Color(0xFF120D0A),
    )
    val modern = LcarsPalette(
        primary = Color(0xFF3A9BDC), secondary = Color(0xFF22C3C3), tertiary = Color(0xFFF2A33A),
        accent = Color(0xFFA8D8F0), highlight = Color(0xFF5577FF), alert = Color(0xFFE8403A),
        text = Color(0xFFD6EEFF), textDim = Color(0xFF7FA3BF), panel = Color(0xFF07111A),
    )
    val redAlert = LcarsPalette(
        primary = Color(0xFFCC2222), secondary = Color(0xFFFF5555), tertiary = Color(0xFF992222),
        accent = Color(0xFFFF8877), highlight = Color(0xFFEE3333), alert = Color(0xFFFF2222),
        text = Color(0xFFFFB0A8), textDim = Color(0xFFB06060), panel = Color(0xFF1A0505),
    )

    fun forTheme(id: LcarsThemeId) = when (id) {
        LcarsThemeId.ENTERPRISE_D -> enterpriseD
        LcarsThemeId.VOYAGER -> voyager
        LcarsThemeId.DEEP_SPACE_NINE -> deepSpaceNine
        LcarsThemeId.MODERN -> modern
    }
}

val LcarsFont = FontFamily(
    Font(R.font.antonio, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.antonio, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

val LocalPalette = staticCompositionLocalOf { LcarsPalettes.enterpriseD }
val LocalLcarsSettings = staticCompositionLocalOf { LcarsSettings() }
val LocalSfx = staticCompositionLocalOf<(Sfx) -> Unit> { {} }

/** LCARS text: condensed Antonio typeface, scaled by the Large Text accessibility option. */
@Composable
fun LText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 18.sp,
    color: Color = LocalPalette.current.text,
    bold: Boolean = false,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
    letterSpacing: TextUnit = 0.5.sp,
) {
    val scale = if (LocalLcarsSettings.current.largeText) 1.2f else 1f
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = LcarsFont,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontSize = size * scale,
            color = color,
            textAlign = align,
            letterSpacing = letterSpacing,
        ),
    )
}

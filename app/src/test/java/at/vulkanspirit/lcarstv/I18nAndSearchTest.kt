package at.vulkanspirit.lcarstv

import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.AppLanguage
import at.vulkanspirit.lcarstv.data.ColorMath
import at.vulkanspirit.lcarstv.data.Recents
import at.vulkanspirit.lcarstv.data.StationCommand
import at.vulkanspirit.lcarstv.i18n.De
import at.vulkanspirit.lcarstv.i18n.En
import at.vulkanspirit.lcarstv.i18n.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class I18nAndSearchTest {
    @Test fun languageSelection() {
        assertEquals(De, Strings.forLanguage(AppLanguage.SYSTEM, Locale.GERMANY))
        assertEquals(De, Strings.forLanguage(AppLanguage.SYSTEM, Locale("de", "AT")))
        assertEquals(En, Strings.forLanguage(AppLanguage.SYSTEM, Locale.US))
        assertEquals(En, Strings.forLanguage(AppLanguage.SYSTEM, Locale.FRANCE))
        assertEquals(En, Strings.forLanguage(AppLanguage.ENGLISH, Locale.GERMANY))
        assertEquals(De, Strings.forLanguage(AppLanguage.GERMAN, Locale.US))
    }

    @Test fun germanIsActuallyTranslated() {
        assertEquals("HAUPTBRÜCKE", De.deckBridge)
        assertEquals("STERNZEIT", De.stardate)
        assertEquals("WIEN", De.city("VIENNA"))
        assertEquals("BERLIN", De.city("BERLIN"))
        assertEquals("WLAN", De.network("WI-FI"))
        assertEquals("ETHERNET", De.network("ETHERNET"))
        assertEquals(En.spaceFacts.size, De.spaceFacts.size)
        // Every per-item text differs between the languages for labels that are not loanwords.
        StationCommand.entries.filter { it != StationCommand.STORAGE && it != StationCommand.SOUND }.forEach {
            assertNotEquals(it.name, En.command(it), De.command(it))
        }
        AppGroup.entries.forEach { assertTrue(De.groupRow(it).isNotBlank()) }
        assertEquals("SPEICHER", De.command(StationCommand.STORAGE))
    }

    @Test fun stationCommandsMatchEitherLanguage() {
        assertTrue(StationCommand.WIFI in StationCommand.match("wlan"))
        assertTrue(StationCommand.WIFI in StationCommand.match("WiFi"))
        assertTrue(StationCommand.RED_ALERT in StationCommand.match("roter"))
        assertTrue(StationCommand.RED_ALERT in StationCommand.match("red alert"))
        assertTrue(StationCommand.SOUND in StationCommand.match("lautst"))
        assertTrue(StationCommand.match("star trek").isEmpty())
        assertTrue(StationCommand.match("s").isEmpty())
    }

    @Test fun recentsAreNewestFirstAndCapped() {
        assertEquals(listOf("b", "a"), Recents.record(listOf("a"), "b"))
        assertEquals(listOf("a", "b"), Recents.record(listOf("b", "a"), "a"))
        val many = (1..20).map { "p$it" }
        val r = Recents.record(many, "new")
        assertEquals(Recents.MAX, r.size)
        assertEquals("new", r.first())
    }

    @Test fun dominantColourPrefersVividPixels() {
        val red = 0xFFE50914.toInt()
        val black = 0xFF000000.toInt()
        val white = 0xFFFFFFFF.toInt()
        val pixels = IntArray(100) { if (it < 20) red else if (it < 60) black else white }
        val c = ColorMath.dominant(pixels)!!
        assertEquals(0xE5, (c shr 16) and 0xFF)
        assertTrue(((c shr 8) and 0xFF) < 0x20)
        assertNull(ColorMath.dominant(IntArray(10) { white }))
        assertNull(ColorMath.dominant(IntArray(10) { 0 }))
    }
}

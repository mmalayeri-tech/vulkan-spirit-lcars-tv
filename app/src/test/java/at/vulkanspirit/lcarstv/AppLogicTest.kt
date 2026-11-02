package at.vulkanspirit.lcarstv

import at.vulkanspirit.lcarstv.data.AppCategorizer
import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.AppSearch
import at.vulkanspirit.lcarstv.data.Favorites
import at.vulkanspirit.lcarstv.data.after
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLogicTest {
    private val none = AppCategorizer.CATEGORY_UNDEFINED

    @Test fun streamingAppsAreVideo() {
        assertEquals(AppGroup.VIDEO, AppCategorizer.classify("com.netflix.ninja", "Netflix", none))
        assertEquals(AppGroup.VIDEO, AppCategorizer.classify("com.google.android.youtube.tv", "YouTube", none))
        assertEquals(AppGroup.VIDEO, AppCategorizer.classify("com.disney.disneyplus", "Disney+", none))
        assertEquals(AppGroup.VIDEO, AppCategorizer.classify("com.plexapp.android", "Plex", none))
        assertEquals(AppGroup.VIDEO, AppCategorizer.classify("org.videolan.vlc", "VLC", none))
        assertEquals(AppGroup.VIDEO, AppCategorizer.classify("com.example.unknown", "Thing", AppCategorizer.CATEGORY_VIDEO))
    }

    @Test fun otherGroups() {
        assertEquals(AppGroup.MUSIC, AppCategorizer.classify("com.spotify.tv.android", "Spotify", none))
        assertEquals(AppGroup.GAMES, AppCategorizer.classify("com.example.x", "X", AppCategorizer.CATEGORY_GAME))
        assertEquals(AppGroup.SYSTEM, AppCategorizer.classify("com.android.tv.settings", "Settings", none))
        assertEquals(AppGroup.COMMUNICATION, AppCategorizer.classify("com.example.chat", "Chat", AppCategorizer.CATEGORY_SOCIAL))
        assertEquals(AppGroup.OTHER, AppCategorizer.classify("com.example.calc", "Calculator", none))
    }

    @Test fun communicationsDeckIncludesStreamingAndComms() {
        assertTrue(AppCategorizer.isCommunicationsDeck(AppGroup.VIDEO))
        assertTrue(AppCategorizer.isCommunicationsDeck(AppGroup.COMMUNICATION))
        assertFalse(AppCategorizer.isCommunicationsDeck(AppGroup.SYSTEM))
    }

    private data class App(val label: String, val pkg: String)
    private val apps = listOf(
        App("Prime Video", "com.amazon.amazonvideo.livingroom"),
        App("Plex", "com.plexapp.android"),
        App("YouTube", "com.google.android.youtube.tv"),
        App("Spotify", "com.spotify.tv.android"),
    )

    @Test fun searchRanksPrefixFirst() {
        val r = AppSearch.search(apps, "p", { it.label }, { it.pkg })
        assertEquals(listOf("Prime Video", "Plex", "Spotify"), r.map { it.label }.take(3))
    }

    @Test fun searchMatchesWordsAndPackages() {
        assertEquals("Prime Video", AppSearch.search(apps, "vid", { it.label }, { it.pkg }).first().label)
        assertEquals("Prime Video", AppSearch.search(apps, "amazon", { it.label }, { it.pkg }).single().label)
        assertEquals(apps, AppSearch.search(apps, "  ", { it.label }, { it.pkg }))
        assertTrue(AppSearch.search(apps, "zzz", { it.label }, { it.pkg }).isEmpty())
    }

    @Test fun favoritesToggleAndReorder() {
        var f = Favorites.toggle(emptyList(), "a")
        f = Favorites.toggle(f, "b")
        f = Favorites.toggle(f, "c")
        assertEquals(listOf("a", "b", "c"), f)
        assertEquals(listOf("b", "a", "c"), Favorites.moveUp(f, "b"))
        assertEquals(f, Favorites.moveUp(f, "a"))
        assertEquals(listOf("a", "c", "b"), Favorites.moveDown(f, "b"))
        assertEquals(f, Favorites.moveDown(f, "c"))
        assertEquals(listOf("a", "c"), Favorites.toggle(f, "b"))
        assertEquals(listOf("a", "c"), Favorites.prune(f, setOf("a", "c", "z")))
    }

    @Test fun cyclingWraps() {
        assertEquals(2, listOf(1, 2, 3).after(1))
        assertEquals(1, listOf(1, 2, 3).after(3))
    }
}

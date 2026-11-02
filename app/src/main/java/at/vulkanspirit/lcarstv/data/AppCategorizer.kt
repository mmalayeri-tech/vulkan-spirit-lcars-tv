package at.vulkanspirit.lcarstv.data

/** Broad groups used to sort installed apps into LCARS sections. */
enum class AppGroup(val label: String) {
    VIDEO("VIDEO"),
    MUSIC("AUDIO"),
    COMMUNICATION("COMMS"),
    GAMES("GAMES"),
    SYSTEM("SYSTEM"),
    OTHER("OTHER"),
}

/**
 * Classifies an installed app from the metadata the app itself declares
 * (ApplicationInfo.category) plus keyword hints in its package name and label.
 * Nothing here decides *which* apps exist; that always comes from PackageManager.
 */
object AppCategorizer {
    // Mirrors of android.content.pm.ApplicationInfo.CATEGORY_* so this stays plain Kotlin.
    const val CATEGORY_UNDEFINED = -1
    const val CATEGORY_GAME = 0
    const val CATEGORY_AUDIO = 1
    const val CATEGORY_VIDEO = 2
    const val CATEGORY_IMAGE = 3
    const val CATEGORY_SOCIAL = 4
    const val CATEGORY_NEWS = 5

    private val systemHints = listOf(
        "settings", "filemanager", "file manager", "files", "updater", "launcher",
        "installer", "vending", "play store", "downloader", "packageinstaller", "cleaner",
    )
    private val commsHints = listOf(
        "zoom", "meet", "duo", "whatsapp", "telegram", "skype", "teams", "discord",
        "messenger", "mail", "browser", "chrome", "firefox", "news", "facebook", "signal",
    )
    private val musicHints = listOf(
        "spotify", "music", "deezer", "tidal", "soundcloud", "radio", "podcast", "audio",
        "amazon.mp3", "pandora",
    )
    private val gameHints = listOf(
        "game", "stadia", "geforce", "xbox", "steam", "playstation", "arcade", "emulator",
    )
    private val videoHints = listOf(
        "netflix", "youtube", "disney", "primevideo", "amazon.avod", "plex", "vlc", "kodi",
        "hulu", "hbo", "twitch", "dazn", "joyn", "zattoo", "magenta", "waipu", "crunchyroll",
        "paramount", "peacock", "pluto", "iptv", "video", "player", "stream", "movie",
        "film", "cinema", "media", "jellyfin", "emby", "mubi", "apple.atve", "tvplus", "skyshowtime",
    )

    fun classify(packageName: String, label: String, androidCategory: Int): AppGroup {
        val haystack = "${packageName.lowercase()} ${label.lowercase()}"
        fun any(hints: List<String>) = hints.any { haystack.contains(it) }

        return when {
            androidCategory == CATEGORY_GAME || any(gameHints) -> AppGroup.GAMES
            any(systemHints) -> AppGroup.SYSTEM
            any(musicHints) || androidCategory == CATEGORY_AUDIO -> AppGroup.MUSIC
            any(videoHints) || androidCategory == CATEGORY_VIDEO -> AppGroup.VIDEO
            any(commsHints) || androidCategory == CATEGORY_SOCIAL || androidCategory == CATEGORY_NEWS ->
                AppGroup.COMMUNICATION
            else -> AppGroup.OTHER
        }
    }

    /** Apps shown on the COMMUNICATIONS deck: streaming plus communication apps. */
    fun isCommunicationsDeck(group: AppGroup) =
        group == AppGroup.VIDEO || group == AppGroup.MUSIC || group == AppGroup.COMMUNICATION
}

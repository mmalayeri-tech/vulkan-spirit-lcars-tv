package at.vulkanspirit.lcarstv.data

/**
 * Things the search can do besides finding apps. Keywords are English and German together, so
 * either language finds them whatever the interface language is. [settingsAction] is the
 * android.provider.Settings action to open (null = handled inside the app).
 */
enum class StationCommand(val keywords: List<String>, val settingsAction: String?) {
    RED_ALERT(listOf("red alert", "alert", "battle", "roter alarm", "alarm"), null),
    WIFI(listOf("wifi", "wi-fi", "wlan", "network", "internet", "ethernet", "netzwerk"), "android.settings.WIFI_SETTINGS"),
    BLUETOOTH(listOf("bluetooth", "remote", "controller", "gamepad", "headphones", "fernbedienung", "kopfhörer"), "android.settings.BLUETOOTH_SETTINGS"),
    DISPLAY(listOf("display", "picture", "resolution", "hdr", "screen", "bild", "auflösung", "bildschirm"), "android.settings.DISPLAY_SETTINGS"),
    SOUND(listOf("sound", "audio", "volume", "dolby", "ton", "lautstärke"), "android.settings.SOUND_SETTINGS"),
    APPS(listOf("apps", "applications", "uninstall", "anwendungen", "deinstallieren"), "android.settings.APPLICATION_SETTINGS"),
    STORAGE(listOf("storage", "space", "usb", "speicher"), "android.settings.INTERNAL_STORAGE_SETTINGS"),
    DATE(listOf("date", "time", "clock", "datum", "zeit", "uhr"), "android.settings.DATE_SETTINGS"),
    LANGUAGE(listOf("language", "locale", "sprache"), "android.settings.LOCALE_SETTINGS"),
    ACCESSIBILITY(listOf("accessibility", "talkback", "captions", "bedienungshilfen", "untertitel"), "android.settings.ACCESSIBILITY_SETTINGS"),
    ANDROID_SETTINGS(listOf("settings", "android", "system", "einstellungen"), "android.settings.SETTINGS");

    companion object {
        /** Commands whose keywords start with, or begin a word in, the query (min. 2 characters). */
        fun match(query: String): List<StationCommand> {
            val q = query.trim().lowercase()
            if (q.length < 2) return emptyList()
            return entries.filter { c ->
                c.keywords.any { k -> k.startsWith(q) || q.startsWith(k) || k.split(' ').any { it.startsWith(q) } }
            }
        }
    }
}

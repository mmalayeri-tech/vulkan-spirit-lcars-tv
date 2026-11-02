package at.vulkanspirit.lcarstv.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LcarsThemeId(val label: String) {
    ENTERPRISE_D("ENTERPRISE-D"),
    VOYAGER("VOYAGER"),
    DEEP_SPACE_NINE("DEEP SPACE NINE"),
    MODERN("MODERN STARFLEET"),
}

data class GeoPreset(val name: String, val latitude: Double, val longitude: Double)

data class ZonePreset(val label: String, val zoneId: String)

object Presets {
    val shipNames = listOf(
        "VULKAN SPIRIT", "ENTERPRISE", "VOYAGER", "DEFIANT", "DISCOVERY",
        "CERRITOS", "PROTOSTAR", "TITAN", "EXCELSIOR", "SARATOGA",
    )
    val registries = listOf("NCC-2026-V", "NCC-1701-D", "NCC-74656", "NX-74205", "NCC-1031", "NCC-75567", "NCC-80102")
    val volumes = listOf(30, 50, 70, 100)

    val locations = listOf(
        GeoPreset("VIENNA", 48.2082, 16.3738),
        GeoPreset("GRAZ", 47.0707, 15.4395),
        GeoPreset("SALZBURG", 47.8095, 13.0550),
        GeoPreset("INNSBRUCK", 47.2692, 11.4041),
        GeoPreset("LINZ", 48.3069, 14.2858),
        GeoPreset("BERLIN", 52.5200, 13.4050),
        GeoPreset("ZURICH", 47.3769, 8.5417),
        GeoPreset("LONDON", 51.5072, -0.1276),
        GeoPreset("PARIS", 48.8566, 2.3522),
        GeoPreset("NEW YORK", 40.7128, -74.0060),
        GeoPreset("SAN FRANCISCO", 37.7749, -122.4194),
        GeoPreset("TOKYO", 35.6762, 139.6503),
        GeoPreset("SYDNEY", -33.8688, 151.2093),
        GeoPreset("DUBAI", 25.2048, 55.2708),
        GeoPreset("TEHRAN", 35.6892, 51.3890),
    )

    val zones = listOf(
        ZonePreset("UTC", "UTC"),
        ZonePreset("VIENNA", "Europe/Vienna"),
        ZonePreset("LONDON", "Europe/London"),
        ZonePreset("NEW YORK", "America/New_York"),
        ZonePreset("SAN FRANCISCO", "America/Los_Angeles"),
        ZonePreset("TOKYO", "Asia/Tokyo"),
        ZonePreset("SYDNEY", "Australia/Sydney"),
        ZonePreset("DUBAI", "Asia/Dubai"),
        ZonePreset("TEHRAN", "Asia/Tehran"),
        ZonePreset("MOSCOW", "Europe/Moscow"),
        ZonePreset("BEIJING", "Asia/Shanghai"),
        ZonePreset("SAO PAULO", "America/Sao_Paulo"),
        ZonePreset("HONOLULU", "Pacific/Honolulu"),
        ZonePreset("REYKJAVIK", "Atlantic/Reykjavik"),
    )

    val defaultZones = listOf("UTC", "Europe/Vienna", "Europe/London", "America/New_York", "America/Los_Angeles", "Asia/Tokyo")
}

data class LcarsSettings(
    val shipName: String = "VULKAN SPIRIT",
    val registry: String = "NCC-2026-V",
    val theme: LcarsThemeId = LcarsThemeId.ENTERPRISE_D,
    val soundsEnabled: Boolean = true,
    val navigationSounds: Boolean = false,
    val volume: Int = 70,
    val voiceEnabled: Boolean = false,
    val animationsEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val largeText: Boolean = false,
    val use24h: Boolean = true,
    val weatherEnabled: Boolean = true,
    val weatherLocation: Int = 0,
    val worldClocks: List<String> = Presets.defaultZones,
    val favorites: List<String> = emptyList(),
) {
    /** Motion is shown only when animations are on and reduced motion is off. */
    val motion: Boolean get() = animationsEnabled && !reducedMotion
    val location: GeoPreset get() = Presets.locations[weatherLocation.coerceIn(0, Presets.locations.lastIndex)]
}

/** SharedPreferences-backed settings exposed as a StateFlow. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("lcars_settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<LcarsSettings> = _state.asStateFlow()

    fun update(transform: (LcarsSettings) -> LcarsSettings) {
        val next = transform(_state.value)
        _state.value = next
        save(next)
    }

    fun reset() = update { LcarsSettings() }

    private fun load(): LcarsSettings {
        val d = LcarsSettings()
        return LcarsSettings(
            shipName = prefs.getString("shipName", d.shipName) ?: d.shipName,
            registry = prefs.getString("registry", d.registry) ?: d.registry,
            theme = runCatching { LcarsThemeId.valueOf(prefs.getString("theme", d.theme.name)!!) }.getOrDefault(d.theme),
            soundsEnabled = prefs.getBoolean("sounds", d.soundsEnabled),
            navigationSounds = prefs.getBoolean("navSounds", d.navigationSounds),
            volume = prefs.getInt("volume", d.volume),
            voiceEnabled = prefs.getBoolean("voice", d.voiceEnabled),
            animationsEnabled = prefs.getBoolean("animations", d.animationsEnabled),
            reducedMotion = prefs.getBoolean("reducedMotion", d.reducedMotion),
            largeText = prefs.getBoolean("largeText", d.largeText),
            use24h = prefs.getBoolean("use24h", d.use24h),
            weatherEnabled = prefs.getBoolean("weather", d.weatherEnabled),
            weatherLocation = prefs.getInt("weatherLocation", d.weatherLocation),
            worldClocks = prefs.getString("worldClocks", null)?.split('|')?.filter { it.isNotBlank() } ?: d.worldClocks,
            favorites = prefs.getString("favorites", null)?.split('|')?.filter { it.isNotBlank() } ?: d.favorites,
        )
    }

    private fun save(s: LcarsSettings) {
        prefs.edit()
            .putString("shipName", s.shipName)
            .putString("registry", s.registry)
            .putString("theme", s.theme.name)
            .putBoolean("sounds", s.soundsEnabled)
            .putBoolean("navSounds", s.navigationSounds)
            .putInt("volume", s.volume)
            .putBoolean("voice", s.voiceEnabled)
            .putBoolean("animations", s.animationsEnabled)
            .putBoolean("reducedMotion", s.reducedMotion)
            .putBoolean("largeText", s.largeText)
            .putBoolean("use24h", s.use24h)
            .putBoolean("weather", s.weatherEnabled)
            .putInt("weatherLocation", s.weatherLocation)
            .putString("worldClocks", s.worldClocks.joinToString("|"))
            .putString("favorites", s.favorites.joinToString("|"))
            .apply()
    }
}

/** Cycles to the element after [current] in [options], wrapping around. */
fun <T> List<T>.after(current: T): T {
    val i = indexOf(current)
    return this[(i + 1).mod(size)]
}

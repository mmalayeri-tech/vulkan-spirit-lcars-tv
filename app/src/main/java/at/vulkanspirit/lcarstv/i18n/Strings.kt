package at.vulkanspirit.lcarstv.i18n

import at.vulkanspirit.lcarstv.data.AppGroup
import at.vulkanspirit.lcarstv.data.AppLanguage
import at.vulkanspirit.lcarstv.data.StationCommand
import java.util.Locale

/**
 * Every user-visible text in the interface. [En] and [De] implement all of it, so a missing
 * German translation is a compile error rather than an English word slipping through.
 * LCARS displays are upper case; spoken lines (voice*) are sentence case for text-to-speech.
 */
sealed interface Strings {
    val locale: Locale

    // ── Decks ──
    val deckBridge: String
    val deckCommunications: String
    val deckHolodeck: String
    val deckEngineering: String
    val deckAstrometrics: String
    val deckRedAlert: String
    val deckSearch: String
    val deckSettings: String

    // ── Frame / home ──
    val welcomeAboard: String
    val stardate: String
    val stardateShort: String
    val conditionGreen: String
    val conditionRed: String
    val rowFavorites: String
    val rowRecent: String
    val rowStation: String
    val noFavoritesHint: String
    val scanning: String
    val pressOk: String
    val pinnedBadge: String
    val phoneApp: String
    val phoneTag: String
    val androidSettings: String
    fun groupRow(g: AppGroup): String
    fun groupShort(g: AppGroup): String
    val filterAll: String

    // ── App options ──
    val actOpen: String
    val actPin: String
    val actUnpin: String
    val actMoveLeft: String
    val actMoveRight: String
    val actForget: String
    val actAppInfo: String
    val actCancel: String
    fun pinned(app: String): String
    fun unpinned(app: String): String
    fun unableToLaunch(app: String): String

    // ── Communications / Holodeck ──
    fun channelsOpen(n: Int): String
    val holdOkHint: String
    val noCommsApps: String
    fun programsCount(n: Int): String
    val noHoloApps: String

    // ── Search ──
    val searchPrompt: String
    val awaitingInput: String
    val keySpace: String
    val keyDelete: String
    val keyClear: String
    val voiceInput: String
    val rescan: String
    val rescanned: String
    val quickAccess: String
    fun resultsPrograms(n: Int): String
    fun searchContentFor(q: String): String
    val everywhere: String
    val playStore: String
    val commands: String
    fun noMatch(q: String): String
    val noContentSearch: String
    fun command(c: StationCommand): String
    fun openDeck(deck: String): String

    // ── Engineering ──
    val powerMemory: String
    val memoryInUse: String
    val lowMemory: String
    fun storage(label: String): String
    val storageNone: String
    val notAccessible: String
    val cpuCores: String
    val uptime: String
    val subspaceNetwork: String
    val link: String
    fun network(type: String): String
    val internetValidated: String
    val yes: String
    val no: String
    val address: String
    val wifiRate: String
    val wifiSignal: String
    val displayMode: String
    val unknown: String
    val topMode: String
    val noneReported: String
    val hardware: String
    val maker: String
    val model: String
    val deviceProduct: String
    val platform: String
    val securityPatch: String
    val homeApp: String
    val lcarsBuild: String
    val systemChooser: String
    fun targetConfirmed(model: String): String
    fun targetDiffers(target: String, actual: String): String
    val runDiagnostic: String
    val diagnosticComplete: String
    val networkSettings: String
    val storageSettings: String
    val deviceInfo: String

    // ── Astrometrics ──
    val exteriorSensors: String
    val weatherDisabled: String
    fun scanningLocation(name: String): String
    fun weatherFailed(httpCode: Int?): String
    val checkNetwork: String
    fun weather(code: Int): String
    val highLow: String
    val humidity: String
    val wind: String
    val sunriseSunset: String
    val temporalReference: String
    val nextDay: String
    val previousDay: String
    val noWorldClocks: String
    fun moonPhase(index: Int): String
    val illumination: String
    val solarOrbit: String
    val spaceFacts: List<String>
    fun location(name: String): String
    val refreshScan: String
    fun sensors(on: Boolean): String
    fun city(name: String): String

    // ── Red alert ──
    val tacticalCondition: String
    val alertActiveHint: String
    val alertIdleHint: String
    val standDown: String
    val engageRedAlert: String
    val klaxonMuted: String
    val klaxonInfo: String
    val photosensitiveHint: String
    val reducedMotionActive: String
    val testAlertTone: String

    // ── Settings ──
    val configuration: String
    val language: String
    fun languageName(l: AppLanguage): String
    val stationName: String
    val designation: String
    val theme: String
    val soundEffects: String
    val navigationChirps: String
    val effectVolume: String
    val voiceResponses: String
    val onNoTts: String
    val animations: String
    val reducedMotion: String
    val largeText: String
    val clockFormat: String
    val hours24: String
    val hours12: String
    val weatherSensors: String
    val weatherLocation: String
    val worldClocks: String
    fun selectedCount(n: Int): String
    val homeScreenMode: String
    val configure: String
    val androidSystemSettings: String
    val open: String
    val accessibilitySettings: String
    val restoreDefaults: String
    val reset: String
    val settingsRestored: String
    fun onOff(on: Boolean): String
    val renameStation: String
    val confirmName: String
    val chooseRegistered: String
    val worldClocksPick: String
    val done: String
    val shown: String
    val hidden: String
    val homeModeTitle: String
    val homeModeExplanation: String
    val homeEntry: String
    val enabled: String
    val disabled: String
    val currentHome: String
    val openDefaultApps: String
    val homeStatusDefault: String
    val homeStatusSystem: String
    val backToConfiguration: String
    val homeEntryEnabled: String
    val homeEntryDisabled: String
    val systemSettingsUnavailable: String

    // ── Voice ──
    fun voiceWelcome(station: String): String
    fun voiceLaunching(app: String): String
    val voiceUnable: String
    val voiceRedAlert: String
    val voiceStandDown: String
    val voiceEnabled: String
    val voiceWorking: String
    fun voiceMatches(n: Int): String

    companion object {
        fun forLanguage(language: AppLanguage, system: Locale = Locale.getDefault()): Strings = when (language) {
            AppLanguage.ENGLISH -> En
            AppLanguage.GERMAN -> De
            AppLanguage.SYSTEM -> if (system.language == "de") De else En
        }
    }
}

data object En : Strings {
    override val locale: Locale = Locale.UK

    override val deckBridge = "MAIN BRIDGE"
    override val deckCommunications = "COMMUNICATIONS"
    override val deckHolodeck = "HOLODECK"
    override val deckEngineering = "ENGINEERING"
    override val deckAstrometrics = "ASTROMETRICS"
    override val deckRedAlert = "RED ALERT"
    override val deckSearch = "COMPUTER SEARCH"
    override val deckSettings = "SETTINGS"

    override val welcomeAboard = "WELCOME ABOARD"
    override val stardate = "STARDATE"
    override val stardateShort = "SD"
    override val conditionGreen = "CONDITION GREEN"
    override val conditionRed = "CONDITION RED"
    override val rowFavorites = "PRIORITY CHANNELS"
    override val rowRecent = "RECENTLY USED"
    override val rowStation = "STATION CONTROLS"
    override val noFavoritesHint = "NO PRIORITY CHANNELS YET · HOLD OK ON ANY PROGRAM TO PIN IT HERE"
    override val scanning = "SCANNING SUBSPACE FOR PROGRAMS…"
    override val pressOk = "OK  LAUNCH     ·     HOLD OK  OPTIONS"
    override val pinnedBadge = "PRIORITY CHANNEL"
    override val phoneApp = "PHONE APP · MAY NEED A MOUSE"
    override val phoneTag = "PHONE"
    override val androidSettings = "ANDROID SETTINGS"
    override fun groupRow(g: AppGroup) = when (g) {
        AppGroup.VIDEO -> "VIDEO & STREAMING"
        AppGroup.MUSIC -> "MUSIC & AUDIO"
        AppGroup.COMMUNICATION -> "COMMUNICATION & WEB"
        AppGroup.GAMES -> "GAMES"
        AppGroup.SYSTEM -> "SYSTEM & TOOLS"
        AppGroup.OTHER -> "MORE PROGRAMS"
    }
    override fun groupShort(g: AppGroup) = when (g) {
        AppGroup.VIDEO -> "VIDEO"
        AppGroup.MUSIC -> "AUDIO"
        AppGroup.COMMUNICATION -> "COMMS"
        AppGroup.GAMES -> "GAMES"
        AppGroup.SYSTEM -> "SYSTEM"
        AppGroup.OTHER -> "OTHER"
    }
    override val filterAll = "ALL"

    override val actOpen = "OPEN"
    override val actPin = "PIN TO PRIORITY CHANNELS"
    override val actUnpin = "REMOVE FROM PRIORITY CHANNELS"
    override val actMoveLeft = "MOVE LEFT"
    override val actMoveRight = "MOVE RIGHT"
    override val actForget = "REMOVE FROM RECENTLY USED"
    override val actAppInfo = "APP INFO"
    override val actCancel = "CANCEL"
    override fun pinned(app: String) = "$app PINNED"
    override fun unpinned(app: String) = "$app UNPINNED"
    override fun unableToLaunch(app: String) = "UNABLE TO LAUNCH $app"

    override fun channelsOpen(n: Int) = "SUBSPACE CHANNELS OPEN: $n"
    override val holdOkHint = "HOLD OK ON ANY PROGRAM FOR OPTIONS"
    override val noCommsApps = "NO STREAMING OR COMMUNICATION APPS DETECTED.\nSEE HOLODECK FOR ALL PROGRAMS."
    override fun programsCount(n: Int) = "PROGRAMS: $n"
    override val noHoloApps = "NO HOLODECK PROGRAMS IN THIS CATEGORY."

    override val searchPrompt = "COMPUTER, SEARCH FOR…"
    override val awaitingInput = "AWAITING INPUT"
    override val keySpace = "SPACE"
    override val keyDelete = "DEL"
    override val keyClear = "CLEAR"
    override val voiceInput = "VOICE"
    override val rescan = "RESCAN"
    override val rescanned = "PROGRAM DATABASE RESCANNED"
    override val quickAccess = "LIBRARY QUICK ACCESS"
    override fun resultsPrograms(n: Int) = "PROGRAMS · $n"
    override fun searchContentFor(q: String) = "SEARCH FOR “$q” IN"
    override val everywhere = "EVERYWHERE"
    override val playStore = "PLAY STORE"
    override val commands = "STATION COMMANDS"
    override fun noMatch(q: String) = "NO PROGRAM MATCHES “$q”"
    override val noContentSearch = "NO INSTALLED APP OFFERS CONTENT SEARCH"
    override fun command(c: StationCommand) = when (c) {
        StationCommand.RED_ALERT -> "ENGAGE RED ALERT"
        StationCommand.WIFI -> "NETWORK & WI-FI"
        StationCommand.BLUETOOTH -> "BLUETOOTH & REMOTES"
        StationCommand.DISPLAY -> "DISPLAY & PICTURE"
        StationCommand.SOUND -> "SOUND"
        StationCommand.APPS -> "MANAGE APPS"
        StationCommand.STORAGE -> "STORAGE"
        StationCommand.DATE -> "DATE & TIME"
        StationCommand.LANGUAGE -> "SYSTEM LANGUAGE"
        StationCommand.ACCESSIBILITY -> "ACCESSIBILITY"
        StationCommand.ANDROID_SETTINGS -> "ALL ANDROID SETTINGS"
    }
    override fun openDeck(deck: String) = "OPEN $deck"

    override val powerMemory = "POWER & MEMORY"
    override val memoryInUse = "MEMORY IN USE"
    override val lowMemory = "LOW MEMORY CONDITION"
    override fun storage(label: String) = when (label) {
        "INTERNAL" -> "INTERNAL STORAGE"
        "USB" -> "USB STORAGE"
        else -> "$label STORAGE"
    }
    override val storageNone = "STORAGE"
    override val notAccessible = "NOT ACCESSIBLE"
    override val cpuCores = "CPU CORES"
    override val uptime = "UPTIME"
    override val subspaceNetwork = "SUBSPACE NETWORK"
    override val link = "LINK"
    override fun network(type: String) = type
    override val internetValidated = "INTERNET VALIDATED"
    override val yes = "YES"
    override val no = "NO"
    override val address = "ADDRESS"
    override val wifiRate = "WI-FI LINK RATE"
    override val wifiSignal = "WI-FI SIGNAL"
    override val displayMode = "DISPLAY MODE"
    override val unknown = "UNKNOWN"
    override val topMode = "TOP MODE"
    override val noneReported = "NONE REPORTED"
    override val hardware = "HARDWARE"
    override val maker = "MAKER"
    override val model = "MODEL"
    override val deviceProduct = "DEVICE / PRODUCT"
    override val platform = "PLATFORM"
    override val securityPatch = "SECURITY PATCH"
    override val homeApp = "HOME APP"
    override val lcarsBuild = "LCARS BUILD"
    override val systemChooser = "SYSTEM CHOOSER"
    override fun targetConfirmed(model: String) = "TARGET VESSEL $model CONFIRMED"
    override fun targetDiffers(target: String, actual: String) = "TARGET WAS $target; RUNNING ON $actual"
    override val runDiagnostic = "RUN DIAGNOSTIC"
    override val diagnosticComplete = "DIAGNOSTIC COMPLETE"
    override val networkSettings = "NETWORK SETTINGS"
    override val storageSettings = "STORAGE SETTINGS"
    override val deviceInfo = "DEVICE INFO"

    override val exteriorSensors = "EXTERIOR SENSORS"
    override val weatherDisabled = "WEATHER SENSORS DISABLED IN SETTINGS"
    override fun scanningLocation(name: String) = "SCANNING $name…"
    override fun weatherFailed(httpCode: Int?) = if (httpCode != null) "SENSOR ERROR $httpCode" else "LONG-RANGE SENSORS OFFLINE"
    override val checkNetwork = "CHECK NETWORK CONNECTION"
    override fun weather(code: Int) = when (code) {
        0 -> "CLEAR SKY"
        1 -> "MAINLY CLEAR"
        2 -> "PARTLY CLOUDY"
        3 -> "OVERCAST"
        45, 48 -> "FOG"
        51, 53, 55 -> "DRIZZLE"
        56, 57 -> "FREEZING DRIZZLE"
        61, 63, 65 -> "RAIN"
        66, 67 -> "FREEZING RAIN"
        71, 73, 75 -> "SNOWFALL"
        77 -> "SNOW GRAINS"
        80, 81, 82 -> "RAIN SHOWERS"
        85, 86 -> "SNOW SHOWERS"
        95 -> "THUNDERSTORM"
        96, 99 -> "THUNDERSTORM WITH HAIL"
        else -> "UNKNOWN CONDITIONS"
    }
    override val highLow = "HIGH / LOW"
    override val humidity = "HUMIDITY"
    override val wind = "WIND"
    override val sunriseSunset = "SUNRISE / SUNSET"
    override val temporalReference = "TEMPORAL REFERENCE"
    override val nextDay = "+1D"
    override val previousDay = "-1D"
    override val noWorldClocks = "NO WORLD CLOCKS SELECTED"
    override fun moonPhase(index: Int) = listOf(
        "NEW MOON", "WAXING CRESCENT", "FIRST QUARTER", "WAXING GIBBOUS",
        "FULL MOON", "WANING GIBBOUS", "LAST QUARTER", "WANING CRESCENT",
    )[index.mod(8)]
    override val illumination = "ILLUMINATION"
    override val solarOrbit = "SOLAR ORBIT PROGRESS"
    override val spaceFacts = listOf(
        "LIGHT FROM THE SUN REACHES EARTH IN ABOUT 8 MINUTES 20 SECONDS.",
        "THE INTERNATIONAL SPACE STATION CIRCLES EARTH ROUGHLY EVERY 90 MINUTES.",
        "A DAY ON VENUS IS LONGER THAN ITS YEAR.",
        "JUPITER'S GREAT RED SPOT IS A STORM LARGER THAN EARTH.",
        "THE VOYAGER 1 PROBE IS THE MOST DISTANT HUMAN-MADE OBJECT.",
        "THE MOON DRIFTS AWAY FROM EARTH BY ABOUT 3.8 CM EACH YEAR.",
        "NEUTRON STARS CAN SPIN HUNDREDS OF TIMES PER SECOND.",
        "SATURN WOULD FLOAT IN WATER: ITS AVERAGE DENSITY IS BELOW 1 G/CM3.",
        "THE MILKY WAY CONTAINS AN ESTIMATED 100 TO 400 BILLION STARS.",
        "ALPHA CENTAURI IS THE NEAREST STAR SYSTEM, ABOUT 4.37 LIGHT-YEARS AWAY.",
    )
    override fun location(name: String) = "LOCATION: $name"
    override val refreshScan = "REFRESH SCAN"
    override fun sensors(on: Boolean) = if (on) "SENSORS: ON" else "SENSORS: OFF"
    override fun city(name: String) = name

    override val tacticalCondition = "TACTICAL CONDITION"
    override val alertActiveHint = "ALL HANDS TO BATTLE STATIONS. PRESS BACK OR STAND DOWN TO CANCEL."
    override val alertIdleHint = "ENGAGE TO SOUND GENERAL QUARTERS ACROSS EVERY DECK OF THE INTERFACE."
    override val standDown = "STAND DOWN"
    override val engageRedAlert = "ENGAGE RED ALERT"
    override val klaxonMuted = "ALERT KLAXON MUTED (SOUND EFFECTS OFF)"
    override val klaxonInfo = "KLAXON SOUNDS SIX TIMES, THEN THE VISUAL ALERT CONTINUES SILENTLY"
    override val photosensitiveHint = "PHOTOSENSITIVE? ENABLE REDUCED MOTION IN SETTINGS FOR A STEADY, NON-PULSING ALERT."
    override val reducedMotionActive = "REDUCED MOTION ACTIVE: ALERT IS SHOWN WITHOUT PULSING."
    override val testAlertTone = "TEST ALERT TONE"

    override val configuration = "CONFIGURATION"
    override val language = "LANGUAGE / SPRACHE"
    override fun languageName(l: AppLanguage) = when (l) {
        AppLanguage.SYSTEM -> "AUTOMATIC (TV LANGUAGE)"
        AppLanguage.ENGLISH -> "ENGLISH"
        AppLanguage.GERMAN -> "DEUTSCH"
    }
    override val stationName = "STATION NAME"
    override val designation = "DESIGNATION"
    override val theme = "INTERFACE THEME"
    override val soundEffects = "SOUND EFFECTS"
    override val navigationChirps = "NAVIGATION CHIRPS"
    override val effectVolume = "EFFECT VOLUME"
    override val voiceResponses = "VOICE RESPONSES"
    override val onNoTts = "ON (NO TTS ENGINE)"
    override val animations = "ANIMATIONS"
    override val reducedMotion = "REDUCED MOTION / NO FLASHING"
    override val largeText = "LARGE TEXT"
    override val clockFormat = "CLOCK FORMAT"
    override val hours24 = "24 HOUR"
    override val hours12 = "12 HOUR"
    override val weatherSensors = "WEATHER SENSORS"
    override val weatherLocation = "WEATHER LOCATION"
    override val worldClocks = "WORLD CLOCKS"
    override fun selectedCount(n: Int) = "$n SELECTED"
    override val homeScreenMode = "HOME SCREEN MODE (OPTIONAL)"
    override val configure = "CONFIGURE"
    override val androidSystemSettings = "ANDROID SYSTEM SETTINGS"
    override val open = "OPEN"
    override val accessibilitySettings = "ACCESSIBILITY SETTINGS"
    override val restoreDefaults = "RESTORE DEFAULTS"
    override val reset = "RESET"
    override val settingsRestored = "SETTINGS RESTORED TO DEFAULTS"
    override fun onOff(on: Boolean) = if (on) "ON" else "OFF"
    override val renameStation = "RENAME STATION"
    override val confirmName = "CONFIRM NAME"
    override val chooseRegistered = "OR CHOOSE A REGISTERED NAME"
    override val worldClocksPick = "WORLD CLOCKS — SELECT UP TO 8"
    override val done = "DONE"
    override val shown = "SHOWN"
    override val hidden = "HIDDEN"
    override val homeModeTitle = "HOME SCREEN MODE — OPTIONAL AND REVERSIBLE"
    override val homeModeExplanation =
        "THIS APP ALWAYS WORKS NORMALLY FROM YOUR APPS ROW. THIS SWITCH ONLY ADVERTISES IT AS A POSSIBLE " +
            "HOME APP. IT NEVER DISABLES THE GOOGLE TV OR XIAOMI LAUNCHER. MANY GOOGLE TV FIRMWARES IGNORE " +
            "THIRD-PARTY HOME APPS, SO THE HOME BUTTON MAY STILL OPEN GOOGLE TV."
    override val homeEntry = "LCARS HOME ENTRY"
    override val enabled = "ENABLED"
    override val disabled = "DISABLED"
    override val currentHome = "CURRENT DEFAULT HOME APP"
    override val openDefaultApps = "OPEN DEFAULT APPS SETTINGS"
    override val homeStatusDefault = "STATUS: THIS APP IS THE DEFAULT HOME APP. TO UNDO, SWITCH THE ENTRY OFF ABOVE."
    override val homeStatusSystem = "STATUS: SYSTEM LAUNCHER REMAINS DEFAULT. SEE docs/HOME_LAUNCHER.md BEFORE TRYING ANYTHING MORE."
    override val backToConfiguration = "BACK TO CONFIGURATION"
    override val homeEntryEnabled = "HOME MODE ENTRY ENABLED"
    override val homeEntryDisabled = "HOME MODE ENTRY DISABLED"
    override val systemSettingsUnavailable = "SYSTEM SETTINGS UNAVAILABLE"

    override fun voiceWelcome(station: String) = "Welcome aboard ${station.lowercase().replaceFirstChar { it.titlecase() }}"
    override fun voiceLaunching(app: String) = "Launching $app"
    override val voiceUnable = "Unable to comply"
    override val voiceRedAlert = "Red alert. All hands to battle stations."
    override val voiceStandDown = "Condition green. Standing down."
    override val voiceEnabled = "Voice responses enabled"
    override val voiceWorking = "Working"
    override fun voiceMatches(n: Int) = if (n == 1) "One match found" else "$n matches found"
}

data object De : Strings {
    override val locale: Locale = Locale.GERMANY

    override val deckBridge = "HAUPTBRÜCKE"
    override val deckCommunications = "KOMMUNIKATION"
    override val deckHolodeck = "HOLODECK"
    override val deckEngineering = "MASCHINENRAUM"
    override val deckAstrometrics = "ASTROMETRIE"
    override val deckRedAlert = "ROTER ALARM"
    override val deckSearch = "COMPUTERSUCHE"
    override val deckSettings = "EINSTELLUNGEN"

    override val welcomeAboard = "WILLKOMMEN AN BORD"
    override val stardate = "STERNZEIT"
    override val stardateShort = "SZ"
    override val conditionGreen = "ZUSTAND GRÜN"
    override val conditionRed = "ALARMSTUFE ROT"
    override val rowFavorites = "PRIORITÄTSKANÄLE"
    override val rowRecent = "ZULETZT VERWENDET"
    override val rowStation = "STATIONSSTEUERUNG"
    override val noFavoritesHint = "NOCH KEINE PRIORITÄTSKANÄLE · OK GEDRÜCKT HALTEN, UM EIN PROGRAMM HIER ANZUHEFTEN"
    override val scanning = "SUBRAUMSCAN NACH PROGRAMMEN…"
    override val pressOk = "OK  STARTEN     ·     OK HALTEN  OPTIONEN"
    override val pinnedBadge = "PRIORITÄTSKANAL"
    override val phoneApp = "SMARTPHONE-APP · BRAUCHT EVTL. EINE MAUS"
    override val phoneTag = "HANDY"
    override val androidSettings = "ANDROID-EINSTELLUNGEN"
    override fun groupRow(g: AppGroup) = when (g) {
        AppGroup.VIDEO -> "VIDEO & STREAMING"
        AppGroup.MUSIC -> "MUSIK & AUDIO"
        AppGroup.COMMUNICATION -> "KOMMUNIKATION & WEB"
        AppGroup.GAMES -> "SPIELE"
        AppGroup.SYSTEM -> "SYSTEM & WERKZEUGE"
        AppGroup.OTHER -> "WEITERE PROGRAMME"
    }
    override fun groupShort(g: AppGroup) = when (g) {
        AppGroup.VIDEO -> "VIDEO"
        AppGroup.MUSIC -> "AUDIO"
        AppGroup.COMMUNICATION -> "KOMM."
        AppGroup.GAMES -> "SPIELE"
        AppGroup.SYSTEM -> "SYSTEM"
        AppGroup.OTHER -> "ANDERE"
    }
    override val filterAll = "ALLE"

    override val actOpen = "ÖFFNEN"
    override val actPin = "ALS PRIORITÄTSKANAL ANHEFTEN"
    override val actUnpin = "AUS PRIORITÄTSKANÄLEN ENTFERNEN"
    override val actMoveLeft = "NACH LINKS"
    override val actMoveRight = "NACH RECHTS"
    override val actForget = "AUS „ZULETZT VERWENDET“ ENTFERNEN"
    override val actAppInfo = "APP-INFO"
    override val actCancel = "ABBRECHEN"
    override fun pinned(app: String) = "$app ANGEHEFTET"
    override fun unpinned(app: String) = "$app ENTFERNT"
    override fun unableToLaunch(app: String) = "$app KANN NICHT GESTARTET WERDEN"

    override fun channelsOpen(n: Int) = "OFFENE SUBRAUMKANÄLE: $n"
    override val holdOkHint = "OK GEDRÜCKT HALTEN FÜR OPTIONEN"
    override val noCommsApps = "KEINE STREAMING- ODER KOMMUNIKATIONS-APPS GEFUNDEN.\nALLE PROGRAMME FINDEN SICH IM HOLODECK."
    override fun programsCount(n: Int) = "PROGRAMME: $n"
    override val noHoloApps = "KEINE HOLODECK-PROGRAMME IN DIESER KATEGORIE."

    override val searchPrompt = "COMPUTER, SUCHE NACH…"
    override val awaitingInput = "WARTE AUF EINGABE"
    override val keySpace = "LEER"
    override val keyDelete = "ENTF"
    override val keyClear = "LEEREN"
    override val voiceInput = "SPRACHE"
    override val rescan = "NEU SCANNEN"
    override val rescanned = "PROGRAMMDATENBANK NEU GESCANNT"
    override val quickAccess = "BIBLIOTHEK-SCHNELLZUGRIFF"
    override fun resultsPrograms(n: Int) = "PROGRAMME · $n"
    override fun searchContentFor(q: String) = "„$q“ SUCHEN IN"
    override val everywhere = "ÜBERALL"
    override val playStore = "PLAY STORE"
    override val commands = "STATIONSBEFEHLE"
    override fun noMatch(q: String) = "KEIN PROGRAMM PASST ZU „$q“"
    override val noContentSearch = "KEINE INSTALLIERTE APP BIETET INHALTSSUCHE AN"
    override fun command(c: StationCommand) = when (c) {
        StationCommand.RED_ALERT -> "ROTEN ALARM AUSLÖSEN"
        StationCommand.WIFI -> "NETZWERK & WLAN"
        StationCommand.BLUETOOTH -> "BLUETOOTH & FERNBEDIENUNGEN"
        StationCommand.DISPLAY -> "DISPLAY & BILD"
        StationCommand.SOUND -> "TON"
        StationCommand.APPS -> "APPS VERWALTEN"
        StationCommand.STORAGE -> "SPEICHER"
        StationCommand.DATE -> "DATUM & UHRZEIT"
        StationCommand.LANGUAGE -> "SYSTEMSPRACHE"
        StationCommand.ACCESSIBILITY -> "BEDIENUNGSHILFEN"
        StationCommand.ANDROID_SETTINGS -> "ALLE ANDROID-EINSTELLUNGEN"
    }
    override fun openDeck(deck: String) = "$deck ÖFFNEN"

    override val powerMemory = "ENERGIE & SPEICHER"
    override val memoryInUse = "BELEGTER ARBEITSSPEICHER"
    override val lowMemory = "ARBEITSSPEICHER KNAPP"
    override fun storage(label: String) = when (label) {
        "INTERNAL" -> "INTERNER SPEICHER"
        "USB" -> "USB-SPEICHER"
        else -> "SPEICHER $label"
    }
    override val storageNone = "SPEICHER"
    override val notAccessible = "NICHT VERFÜGBAR"
    override val cpuCores = "CPU-KERNE"
    override val uptime = "BETRIEBSZEIT"
    override val subspaceNetwork = "SUBRAUMNETZ"
    override val link = "VERBINDUNG"
    override fun network(type: String) = when (type) {
        "WI-FI" -> "WLAN"
        "CELLULAR" -> "MOBILFUNK"
        "UNKNOWN" -> "UNBEKANNT"
        "OTHER" -> "ANDERE"
        else -> type
    }
    override val internetValidated = "INTERNET BESTÄTIGT"
    override val yes = "JA"
    override val no = "NEIN"
    override val address = "ADRESSE"
    override val wifiRate = "WLAN-RATE"
    override val wifiSignal = "WLAN-SIGNAL"
    override val displayMode = "ANZEIGEMODUS"
    override val unknown = "UNBEKANNT"
    override val topMode = "HÖCHSTER MODUS"
    override val noneReported = "KEINE GEMELDET"
    override val hardware = "HARDWARE"
    override val maker = "HERSTELLER"
    override val model = "MODELL"
    override val deviceProduct = "GERÄT / PRODUKT"
    override val platform = "PLATTFORM"
    override val securityPatch = "SICHERHEITSPATCH"
    override val homeApp = "STARTBILDSCHIRM"
    override val lcarsBuild = "LCARS-VERSION"
    override val systemChooser = "SYSTEMAUSWAHL"
    override fun targetConfirmed(model: String) = "ZIELSCHIFF $model BESTÄTIGT"
    override fun targetDiffers(target: String, actual: String) = "ZIEL WAR $target; LÄUFT AUF $actual"
    override val runDiagnostic = "DIAGNOSE STARTEN"
    override val diagnosticComplete = "DIAGNOSE ABGESCHLOSSEN"
    override val networkSettings = "NETZWERK"
    override val storageSettings = "SPEICHER"
    override val deviceInfo = "GERÄTEINFO"

    override val exteriorSensors = "AUSSENSENSOREN"
    override val weatherDisabled = "WETTERSENSOREN SIND IN DEN EINSTELLUNGEN AUS"
    override fun scanningLocation(name: String) = "SCANNE $name…"
    override fun weatherFailed(httpCode: Int?) = if (httpCode != null) "SENSORFEHLER $httpCode" else "LANGSTRECKENSENSOREN OFFLINE"
    override val checkNetwork = "NETZWERKVERBINDUNG PRÜFEN"
    override fun weather(code: Int) = when (code) {
        0 -> "KLARER HIMMEL"
        1 -> "ÜBERWIEGEND KLAR"
        2 -> "TEILWEISE BEWÖLKT"
        3 -> "BEDECKT"
        45, 48 -> "NEBEL"
        51, 53, 55 -> "NIESELREGEN"
        56, 57 -> "GEFRIERENDER NIESELREGEN"
        61, 63, 65 -> "REGEN"
        66, 67 -> "GEFRIERENDER REGEN"
        71, 73, 75 -> "SCHNEEFALL"
        77 -> "SCHNEEGRIESEL"
        80, 81, 82 -> "REGENSCHAUER"
        85, 86 -> "SCHNEESCHAUER"
        95 -> "GEWITTER"
        96, 99 -> "GEWITTER MIT HAGEL"
        else -> "UNBEKANNTE BEDINGUNGEN"
    }
    override val highLow = "HOCH / TIEF"
    override val humidity = "LUFTFEUCHTE"
    override val wind = "WIND"
    override val sunriseSunset = "SONNENAUF- / UNTERGANG"
    override val temporalReference = "ZEITREFERENZ"
    override val nextDay = "+1T"
    override val previousDay = "-1T"
    override val noWorldClocks = "KEINE WELTUHREN GEWÄHLT"
    override fun moonPhase(index: Int) = listOf(
        "NEUMOND", "ZUNEHMENDE SICHEL", "ERSTES VIERTEL", "ZUNEHMENDER MOND",
        "VOLLMOND", "ABNEHMENDER MOND", "LETZTES VIERTEL", "ABNEHMENDE SICHEL",
    )[index.mod(8)]
    override val illumination = "BELEUCHTUNG"
    override val solarOrbit = "SONNENUMLAUF"
    override val spaceFacts = listOf(
        "SONNENLICHT BRAUCHT ETWA 8 MINUTEN 20 SEKUNDEN BIS ZUR ERDE.",
        "DIE INTERNATIONALE RAUMSTATION UMRUNDET DIE ERDE ETWA ALLE 90 MINUTEN.",
        "EIN TAG AUF DER VENUS IST LÄNGER ALS IHR JAHR.",
        "JUPITERS GROSSER ROTER FLECK IST EIN STURM, GRÖSSER ALS DIE ERDE.",
        "DIE SONDE VOYAGER 1 IST DAS ENTFERNTESTE VON MENSCHEN GEBAUTE OBJEKT.",
        "DER MOND ENTFERNT SICH JEDES JAHR UM ETWA 3,8 CM VON DER ERDE.",
        "NEUTRONENSTERNE KÖNNEN SICH HUNDERTE MALE PRO SEKUNDE DREHEN.",
        "SATURN WÜRDE IM WASSER SCHWIMMEN: SEINE MITTLERE DICHTE LIEGT UNTER 1 G/CM3.",
        "DIE MILCHSTRASSE ENTHÄLT SCHÄTZUNGSWEISE 100 BIS 400 MILLIARDEN STERNE.",
        "ALPHA CENTAURI IST DAS NÄCHSTE STERNSYSTEM, ETWA 4,37 LICHTJAHRE ENTFERNT.",
    )
    override fun location(name: String) = "ORT: $name"
    override val refreshScan = "NEU SCANNEN"
    override fun sensors(on: Boolean) = if (on) "SENSOREN: AN" else "SENSOREN: AUS"
    override fun city(name: String) = when (name) {
        "VIENNA" -> "WIEN"
        "ZURICH" -> "ZÜRICH"
        "TOKYO" -> "TOKIO"
        "TEHRAN" -> "TEHERAN"
        "MOSCOW" -> "MOSKAU"
        "BEIJING" -> "PEKING"
        else -> name
    }

    override val tacticalCondition = "TAKTISCHER ZUSTAND"
    override val alertActiveHint = "ALLE MANN AUF GEFECHTSSTATION. ZURÜCK ODER ENTWARNUNG ZUM BEENDEN."
    override val alertIdleHint = "LÖST DEN GEFECHTSALARM AUF ALLEN DECKS DER OBERFLÄCHE AUS."
    override val standDown = "ENTWARNUNG"
    override val engageRedAlert = "ROTEN ALARM AUSLÖSEN"
    override val klaxonMuted = "ALARMSIRENE STUMM (SOUNDEFFEKTE AUS)"
    override val klaxonInfo = "DIE SIRENE ERTÖNT SECHSMAL, DANACH LÄUFT DER SICHTBARE ALARM LAUTLOS WEITER"
    override val photosensitiveHint = "LICHTEMPFINDLICH? „WENIGER BEWEGUNG“ IN DEN EINSTELLUNGEN ZEIGT DEN ALARM OHNE PULSIEREN."
    override val reducedMotionActive = "WENIGER BEWEGUNG AKTIV: ALARM OHNE PULSIEREN."
    override val testAlertTone = "ALARMTON TESTEN"

    override val configuration = "KONFIGURATION"
    override val language = "SPRACHE / LANGUAGE"
    override fun languageName(l: AppLanguage) = when (l) {
        AppLanguage.SYSTEM -> "AUTOMATISCH (TV-SPRACHE)"
        AppLanguage.ENGLISH -> "ENGLISH"
        AppLanguage.GERMAN -> "DEUTSCH"
    }
    override val stationName = "STATIONSNAME"
    override val designation = "KENNUNG"
    override val theme = "OBERFLÄCHEN-DESIGN"
    override val soundEffects = "SOUNDEFFEKTE"
    override val navigationChirps = "NAVIGATIONSTÖNE"
    override val effectVolume = "EFFEKTLAUTSTÄRKE"
    override val voiceResponses = "SPRACHAUSGABE"
    override val onNoTts = "AN (KEINE SPRACHAUSGABE-ENGINE)"
    override val animations = "ANIMATIONEN"
    override val reducedMotion = "WENIGER BEWEGUNG / KEIN BLINKEN"
    override val largeText = "GROSSE SCHRIFT"
    override val clockFormat = "UHRZEITFORMAT"
    override val hours24 = "24 STUNDEN"
    override val hours12 = "12 STUNDEN"
    override val weatherSensors = "WETTERSENSOREN"
    override val weatherLocation = "WETTERORT"
    override val worldClocks = "WELTUHREN"
    override fun selectedCount(n: Int) = "$n AUSGEWÄHLT"
    override val homeScreenMode = "STARTBILDSCHIRM-MODUS (OPTIONAL)"
    override val configure = "EINRICHTEN"
    override val androidSystemSettings = "ANDROID-SYSTEMEINSTELLUNGEN"
    override val open = "ÖFFNEN"
    override val accessibilitySettings = "BEDIENUNGSHILFEN"
    override val restoreDefaults = "STANDARDWERTE"
    override val reset = "ZURÜCKSETZEN"
    override val settingsRestored = "EINSTELLUNGEN ZURÜCKGESETZT"
    override fun onOff(on: Boolean) = if (on) "AN" else "AUS"
    override val renameStation = "STATION UMBENENNEN"
    override val confirmName = "NAME BESTÄTIGEN"
    override val chooseRegistered = "ODER EINEN REGISTRIERTEN NAMEN WÄHLEN"
    override val worldClocksPick = "WELTUHREN — BIS ZU 8 WÄHLEN"
    override val done = "FERTIG"
    override val shown = "AN"
    override val hidden = "AUS"
    override val homeModeTitle = "STARTBILDSCHIRM-MODUS — OPTIONAL UND UMKEHRBAR"
    override val homeModeExplanation =
        "DIESE APP FUNKTIONIERT IMMER NORMAL AUS DER APP-LEISTE. DIESER SCHALTER MELDET SIE NUR ALS MÖGLICHE " +
            "STARTBILDSCHIRM-APP. DER GOOGLE-TV- ODER XIAOMI-LAUNCHER WIRD NIE DEAKTIVIERT. VIELE GOOGLE-TV-" +
            "FIRMWARES IGNORIEREN FREMDE STARTBILDSCHIRME; DIE HOME-TASTE ÖFFNET DANN WEITER GOOGLE TV."
    override val homeEntry = "LCARS-STARTEINTRAG"
    override val enabled = "AKTIV"
    override val disabled = "INAKTIV"
    override val currentHome = "AKTUELLE STARTBILDSCHIRM-APP"
    override val openDefaultApps = "STANDARD-APPS ÖFFNEN"
    override val homeStatusDefault = "STATUS: DIESE APP IST DER STARTBILDSCHIRM. ZUM RÜCKGÄNGIGMACHEN OBEN AUSSCHALTEN."
    override val homeStatusSystem = "STATUS: DER SYSTEM-LAUNCHER BLEIBT STANDARD. VOR WEITEREN SCHRITTEN docs/HOME_LAUNCHER.md LESEN."
    override val backToConfiguration = "ZURÜCK ZUR KONFIGURATION"
    override val homeEntryEnabled = "STARTEINTRAG AKTIVIERT"
    override val homeEntryDisabled = "STARTEINTRAG DEAKTIVIERT"
    override val systemSettingsUnavailable = "SYSTEMEINSTELLUNGEN NICHT VERFÜGBAR"

    override fun voiceWelcome(station: String) = "Willkommen an Bord: ${station.lowercase().replaceFirstChar { it.titlecase() }}"
    override fun voiceLaunching(app: String) = "Starte $app"
    override val voiceUnable = "Nicht ausführbar"
    override val voiceRedAlert = "Roter Alarm. Alle Mann auf Gefechtsstation."
    override val voiceStandDown = "Zustand grün. Alarm aufgehoben."
    override val voiceEnabled = "Sprachausgabe aktiviert"
    override val voiceWorking = "Wird bearbeitet"
    override fun voiceMatches(n: Int) = if (n == 1) "Ein Treffer gefunden" else "$n Treffer gefunden"
}

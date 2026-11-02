package at.vulkanspirit.lcarstv

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.vulkanspirit.lcarstv.audio.SoundEngine
import at.vulkanspirit.lcarstv.audio.Sfx
import at.vulkanspirit.lcarstv.audio.VoiceEngine
import at.vulkanspirit.lcarstv.data.AppRepository
import at.vulkanspirit.lcarstv.data.Favorites
import at.vulkanspirit.lcarstv.data.InstalledApp
import at.vulkanspirit.lcarstv.data.LcarsSettings
import at.vulkanspirit.lcarstv.data.SettingsStore
import at.vulkanspirit.lcarstv.data.SystemInfoProvider
import at.vulkanspirit.lcarstv.data.SystemSnapshot
import at.vulkanspirit.lcarstv.data.WeatherRepository
import at.vulkanspirit.lcarstv.data.WeatherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LcarsViewModel(app: Application) : AndroidViewModel(app) {
    private val settingsStore = SettingsStore(app)
    val settings: StateFlow<LcarsSettings> = settingsStore.state

    private val appRepository = AppRepository(app)
    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps.asStateFlow()
    private val _appsLoading = MutableStateFlow(true)
    val appsLoading: StateFlow<Boolean> = _appsLoading.asStateFlow()

    private val systemInfo = SystemInfoProvider(app)
    private val _system = MutableStateFlow(SystemSnapshot())
    val system: StateFlow<SystemSnapshot> = _system.asStateFlow()

    private val weatherRepository = WeatherRepository()
    private val _weather = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weather: StateFlow<WeatherState> = _weather.asStateFlow()
    private val weatherRefresh = MutableStateFlow(0)

    private val _redAlert = MutableStateFlow(false)
    val redAlert: StateFlow<Boolean> = _redAlert.asStateFlow()

    /** Emitted when the Home button re-delivers our home intent: jump back to the bridge. */
    private val _homeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homeEvents: SharedFlow<Unit> = _homeEvents

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()
    private var toastJob: Job? = null

    val sounds = SoundEngine()
    val voice = VoiceEngine(app)

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = refreshApps()
    }

    init {
        sounds.prepare()
        ContextCompat.registerReceiver(
            app,
            packageReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            },
            ContextCompat.RECEIVER_EXPORTED,
        )
        refreshApps()
        refreshSystem()

        viewModelScope.launch {
            settings.collect { s ->
                sounds.enabled = s.soundsEnabled
                sounds.navigationSounds = s.navigationSounds
                sounds.volume = s.volume / 100f
                voice.enabled = s.voiceEnabled
            }
        }
        viewModelScope.launch {
            settings.map { it.weatherEnabled to it.weatherLocation }
                .distinctUntilChanged()
                .collectLatest { (enabled, _) ->
                    if (!enabled) {
                        _weather.value = WeatherState.Disabled
                        return@collectLatest
                    }
                    weatherRefresh.collectLatest {
                        while (true) {
                            if (_weather.value !is WeatherState.Ready) _weather.value = WeatherState.Loading
                            _weather.value = weatherRepository.fetch(settings.value.location)
                            delay(if (_weather.value is WeatherState.Ready) 30 * 60_000L else 2 * 60_000L)
                        }
                    }
                }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _appsLoading.value = true
            val list = runCatching { appRepository.loadApps() }.getOrDefault(emptyList())
            _apps.value = list
            _appsLoading.value = false
            if (list.isNotEmpty()) {
                val installed = list.map { it.packageName }.toSet()
                val pruned = Favorites.prune(settings.value.favorites, installed)
                if (pruned != settings.value.favorites) settingsStore.update { it.copy(favorites = pruned) }
            }
        }
    }

    fun refreshSystem() {
        viewModelScope.launch {
            _system.value = withContext(Dispatchers.IO) { runCatching { systemInfo.snapshot() }.getOrDefault(_system.value) }
        }
    }

    fun refreshWeather() {
        weatherRefresh.value++
    }

    fun updateSettings(transform: (LcarsSettings) -> LcarsSettings) = settingsStore.update(transform)

    fun resetSettings() {
        settingsStore.reset()
        showToast("SETTINGS RESTORED TO DEFAULTS")
    }

    fun play(sfx: Sfx) = sounds.play(sfx)

    fun say(text: String) = voice.say(text)

    fun launch(context: Context, app: InstalledApp) {
        if (appRepository.launch(context, app)) {
            sounds.play(Sfx.LAUNCH)
            voice.say("Launching ${app.label}")
        } else {
            sounds.play(Sfx.ERROR)
            voice.say("Unable to comply")
            showToast("UNABLE TO LAUNCH ${app.label.uppercase()}")
        }
    }

    fun toggleFavorite(pkg: String) {
        sounds.play(Sfx.TOGGLE)
        settingsStore.update { it.copy(favorites = Favorites.toggle(it.favorites, pkg)) }
    }

    fun moveFavorite(pkg: String, up: Boolean) = settingsStore.update {
        it.copy(favorites = if (up) Favorites.moveUp(it.favorites, pkg) else Favorites.moveDown(it.favorites, pkg))
    }

    fun setRedAlert(on: Boolean) {
        if (_redAlert.value == on) return
        _redAlert.value = on
        if (on) {
            sounds.play(Sfx.ALERT)
            voice.say("Red alert. All hands to battle stations.")
        } else {
            sounds.stopAlert()
            sounds.play(Sfx.BACK)
            voice.say("Condition green. Standing down.")
        }
    }

    fun onHomeIntent() {
        _homeEvents.tryEmit(Unit)
    }

    /** Called when another app comes to the foreground: keep the alert state but silence the klaxon. */
    fun onBackgrounded() = sounds.stopAlert()

    fun setHomeAlias(enabled: Boolean) {
        runCatching { systemInfo.setHomeAliasEnabled(enabled) }
        refreshSystem()
        showToast(if (enabled) "HOME MODE ENTRY ENABLED" else "HOME MODE ENTRY DISABLED")
    }

    /** Tries each settings screen in turn; firmware often omits some of them. */
    fun openSystemSettings(context: Context, vararg actions: String) {
        val all = actions.toList() + Settings.ACTION_SETTINGS
        for (action in all) {
            val ok = runCatching {
                context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }.isSuccess
            if (ok) return
        }
        sounds.play(Sfx.ERROR)
        showToast("SYSTEM SETTINGS UNAVAILABLE")
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _toast.value = message
        toastJob = viewModelScope.launch {
            delay(3500)
            _toast.value = null
        }
    }

    override fun onCleared() {
        runCatching { getApplication<Application>().unregisterReceiver(packageReceiver) }
        sounds.release()
        voice.shutdown()
    }
}

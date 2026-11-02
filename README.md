# Vulkan Spirit LCARS TV

A native Android TV / Google TV app (Kotlin + Jetpack Compose for TV) that turns a Xiaomi TV Box into a
Star Trek-inspired LCARS computer interface and launches your installed TV apps, all from the remote.

*Made with AI — Ideas by Mohammad J Malayeri.*

Package: `at.vulkanspirit.lcarstv` · minSdk 26 · targetSdk 35 · built and tested for Android 11 (API 30) TV.

> Fan-made, non-commercial interface inspired by LCARS. It is not affiliated with or endorsed by CBS/Paramount.
> All artwork is drawn in code, and all sounds are synthesized in code. The bundled font is Antonio (SIL OFL,
> see `licenses/`).

## Decks

| Deck | What it does |
|---|---|
| **Main Bridge** | Ship name and registry, live clock, date, stardate, sensor sweep, system overview, favourite apps |
| **Communications** | Installed streaming, audio and communication apps |
| **Holodeck** | Every launchable app, with ALL / VIDEO / AUDIO / GAMES / COMMS / OTHER filters. Uses each app's own TV banner |
| **Engineering** | Real memory, storage (internal and USB), network, display/HDR, hardware and Android build info. Refreshes every 2 s |
| **Astrometrics** | Weather (Open-Meteo, no key, optional), world clocks, moon phase, orbit display, stardate, space facts |
| **Red Alert** | Engage or stand down: the whole interface turns red, with pulsing overlay, alert bars, synthesized klaxon and optional voice |
| **Computer Core** | On-screen LCARS keypad search, launch, pin/unpin and reorder favourites, rescan |
| **Settings** | Ship name (keypad or presets), registry, 4 themes, sounds, navigation chirps, volume, voice, animations, reduced motion, large text, 12/24 h clock, weather, world clocks, optional home mode, Android Settings shortcuts, reset |

Themes: **Enterprise-D**, **Voyager**, **Deep Space Nine**, **Modern Starfleet**.

## Build

```bash
./gradlew testDebugUnitTest assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The project opens in Android Studio as-is.

GitHub Actions (`.github/workflows/android.yml`) runs the unit tests, builds the debug APK and uploads it as an
artifact on every push. Publishing a GitHub Release attaches the APK to that release.

## Install on the TV

See **[docs/INSTALL.md](docs/INSTALL.md)**. It works with only an iPhone and the TV remote.

## More docs

- [docs/DEVICE_COMPATIBILITY.md](docs/DEVICE_COMPATIBILITY.md): MiTV-AKRR0 notes and what is device-dependent
- [docs/HOME_LAUNCHER.md](docs/HOME_LAUNCHER.md): optional, reversible home-screen setup

## Design notes

- **Package visibility (Android 11):** `<queries>` for the `LEANBACK_LAUNCHER`, `LAUNCHER` and `HOME` intents.
  The app does not request `QUERY_ALL_PACKAGES`, and no apps are hard-coded.
- **Permissions:** `INTERNET` (weather), `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`. No location, root or system permissions.
- **Signing:** `app/debug.keystore` is a deliberately public debug key. It keeps every CI build installable over the
  last one. Don't use it for a store release.
- **Photosensitivity:** the red-alert pulse runs at about 0.6 Hz (well under 3 flashes/s). *Reduced motion* makes it static.

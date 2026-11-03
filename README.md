# Space Station Aiya Anusha

A native Android TV / Google TV app (Kotlin + Jetpack Compose for TV) that turns a Xiaomi TV Box into a
Star Trek-inspired LCARS computer interface with an Apple TV-style home screen, and launches your installed
TV apps, all from the remote. English and German.

*Ideas by Mohammad J Malayeri.*

Package: `at.vulkanspirit.lcarstv` · minSdk 26 · targetSdk 35 · built and tested for Android 11 (API 30) TV.

> Fan-made, non-commercial interface inspired by LCARS. It is not affiliated with or endorsed by CBS/Paramount.
> All artwork is drawn in code, and all sounds are synthesized in code. The bundled font is Antonio (SIL OFL,
> see `licenses/`).

## Decks

| Deck | What it does |
|---|---|
| **Main Bridge** | Apple TV-style home: a top shelf that previews the focused app in its own colour, then rows of app cards (priority channels, recently used, one row per category) and station controls. With nothing focused, the shelf shows the station name, clock, stardate and weather |
| **Computer Search** | One query searches installed apps, offers to search content for it inside every app that supports search (plus Google TV and Play Store when present), and finds station commands (red alert, Wi-Fi, Bluetooth, display, sound…) in English or German. Star Trek quick-access titles, voice input where the TV has a recognizer, and the remote's Search key opens it |
| **Communications** | Installed streaming, audio and communication apps |
| **Holodeck** | Every launchable app, with ALL / VIDEO / AUDIO / GAMES / COMMS / OTHER filters |
| **Engineering** | Real memory, storage (internal and USB), network, display/HDR, hardware and Android build info. Refreshes every 2 s |
| **Astrometrics** | Weather (Open-Meteo, no key, optional), world clocks, moon phase, orbit display, stardate, space facts |
| **Red Alert** | Engage or stand down: the whole interface turns red, with pulsing overlay, alert bars, synthesized klaxon and optional voice |
| **Settings** | Language (automatic / English / Deutsch), station name and designation, 4 themes, sounds, volume, voice, animations, reduced motion, large text, 12/24 h clock, weather, world clocks, optional home mode, Android Settings shortcuts, reset |

**Hold OK** on any app card for its options: open, pin to / remove from priority channels, move left/right,
remove from recently used, or Android's App info. The deck menu folds into a slim numbered rail while you
browse and unfolds when it has focus; Back from content returns to it.

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

- **Package visibility (Android 11):** `<queries>` for the `LEANBACK_LAUNCHER`, `LAUNCHER` and `HOME` intents, plus
  `SEARCH`, `GLOBAL_SEARCH`, `WEB_SEARCH`, `RECOGNIZE_SPEECH` and `market://` for the computer search.
  The app does not request `QUERY_ALL_PACKAGES`, and no apps are hard-coded.
- **Permissions:** `INTERNET` (weather), `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`. No location, root or system permissions.
- **Signing:** `app/debug.keystore` is a deliberately public debug key. It keeps every CI build installable over the
  last one. Don't use it for a store release.
- **Package name:** still `at.vulkanspirit.lcarstv`, so 1.1.0 installs over 1.0.0 and keeps your settings.
- **Stardate:** a stylistic TNG-look value (1987 = 41000, 1000 per year), not an official formula.
- **Photosensitivity:** the red-alert pulse runs at about 0.6 Hz (well under 3 flashes/s). *Reduced motion* makes it static.

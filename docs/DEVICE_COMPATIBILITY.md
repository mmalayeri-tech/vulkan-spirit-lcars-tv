# Device compatibility: MiTV-AKRR0

## What we know

- You reported the device model as **MiTV-AKRR0**, running **Android TV / Google TV 11 (API 30)**.
- This is **not** the same string as **MiTV-AFKR0**, a different Xiaomi model code. Nothing in this project
  assumes the two are the same device, and nothing is hard-wired to either one.
- I could not verify from an authoritative source which retail product (box or stick, region, generation)
  `MiTV-AKRR0` corresponds to. Treat any claim about its exact hardware as **unverified**.

## How the app handles that uncertainty

- Nothing is model-specific. Apps are discovered at runtime through `PackageManager`
  (`LEANBACK_LAUNCHER` and `LAUNCHER` intents), and hardware facts are read from public APIs.
- **Engineering → Hardware** shows the real `Build.MODEL`, device, product, platform and Android version.
  The model turns green and reads *"TARGET VESSEL MiTV-AKRR0 CONFIRMED"* only if the box reports exactly
  that string. Please send a photo of that panel; it settles the question.
- `minSdk` is 26, `targetSdk` 35. The app is built for and runs on API 30, and the APK is ARM-agnostic
  (pure Kotlin, no native libraries).

## Device-dependent behaviour (cannot be verified without the box)

| Feature | Depends on |
|---|---|
| Home-screen replacement | Firmware. Google TV usually keeps its own Home button handling. See `HOME_LAUNCHER.md` |
| Voice responses | A text-to-speech engine being installed (Google TV normally has one) |
| Wi-Fi link rate / signal | Firmware exposing `WifiManager.connectionInfo`. Shown only when available |
| USB storage listing | Removable volumes reported by `StorageManager` |
| HDR / display modes | What the box reports through `Display.getHdrCapabilities()` / `supportedModes` |
| Some settings shortcuts | Firmware including those settings screens. Falls back to main Settings |
| Hold-OK (long press) | The remote sending a long press. Without it, apps can still be launched; pinning then needs a remote that sends long presses |

## Tested so far

- Compiled and unit-tested (JDK 17, AGP 8.7.3, Gradle 8.11.1).
- Run on a **Google TV emulator (API 31, arm64)**, the closest available image; there is no API 30 Google TV
  image for Apple Silicon. Checked there: every deck opened with the D-pad, app launch and return,
  long-press pin, theme switch, red alert on/off, live weather. Screenshots are in `docs/screenshots/`.
- 1.1.0 on the same emulator: Apple TV-style home and top shelf, folding deck rail, computer search with Star Trek
  shortcuts and the remote's Search key, options sheet (hold OK) with pinning, and German throughout.
  Content search inside third-party apps (Netflix, Paramount+ …) depends on which apps on the box accept a
  search request; the emulator has none, so that part is unverified.
- Not yet run on the physical MiTV-AKRR0. Sound and voice output were not heard (the emulator ran without audio).

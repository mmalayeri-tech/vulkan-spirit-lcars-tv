# Optional: using LCARS as the home screen

**Read this first.** Vulkan Spirit LCARS works as a normal app. This page is optional.
Nothing here has been tested on a MiTV-AKRR0 yet.
The app never disables the Google TV launcher, and nothing here requires root.

## What the app does on its own (safe, reversible)

The manifest has a second entry, `HomeAlias`, with `CATEGORY_HOME`. It ships **disabled**.

LCARS → **Settings → Home screen mode → LCARS home entry** switches this entry on or off.
It only changes this app's own component state. Switch it off again to undo it completely.

When the entry is on, some Android TV firmwares show a "choose home app" prompt the next time you press
Home, or list LCARS under *Settings → Apps → Default apps → Home app*.
**Google TV firmwares usually do neither.** The Home button is handled by the Google TV launcher
(`com.google.android.apps.tv.launcherx`), which then still opens. That is expected and harmless.

The same screen shows **Current default home app**, so you can see what the box reports.

## Step 1: confirm whether your box supports it

1. In LCARS, turn **LCARS home entry** on.
2. Press **Home** on the remote.
   - A chooser appears → your firmware supports it. Pick LCARS ("Always" is reversible, see below).
   - Google TV opens with no chooser → go to step 2 only if you want to go further.
3. LCARS → Settings → Home screen mode → **Open default apps settings**.
   If a *Home app* option lists LCARS, you can select it there.

If neither appears, your firmware does not offer a user-facing way to change the home app.
Stopping here is fine: LCARS still works from the Apps row.

## Step 2 (advanced, optional): ADB from a computer

Only do this if you are comfortable with it. It needs a computer with Android platform-tools,
and is **not** possible from an iPhone alone. It does **not** disable Google TV.

```bash
adb connect <TV-IP-ADDRESS>:5555        # needs Settings → Developer options → USB/Network debugging
adb shell cmd package set-home-activity at.vulkanspirit.lcarstv/.HomeAlias
```

**Undo (the reliable ways):** switch **LCARS home entry** off in LCARS Settings, or uninstall LCARS.
Once the home component is gone, Android falls back to the system launcher.

To point Home back at Google TV with ADB instead, first look up the launcher's exact component name
(it varies by firmware) and then set it:

```bash
adb shell cmd package query-activities -a android.intent.action.MAIN -c android.intent.category.HOME
```

```bash
adb shell cmd package set-home-activity <component-printed-above>
```

Even with this, Google TV may still intercept the Home button on some firmware versions.

**Avoid** guides that tell you to `pm disable-user` or uninstall the Google TV launcher. That can break
setup, account and remote features. Recovering may require a factory reset.

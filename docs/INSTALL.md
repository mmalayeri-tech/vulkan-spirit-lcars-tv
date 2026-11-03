# Installing Space Station Aiya Anusha on your Xiaomi TV Box

You don't need Android Studio, a Mac, a USB cable or an Android phone.
An **iPhone** (or any web browser) plus the TV remote is enough.

---

## 1. Get the APK from GitHub

Every push to `main` builds the app automatically in GitHub Actions.

**Option A: a GitHub Release (recommended, easiest for the TV)**

1. On github.com, open your repository → **Releases** → **Draft a new release**.
2. Create a tag such as `v1.1.0` and click **Publish release**.
3. Wait about 5 minutes. The workflow attaches `AiyaAnusha-debug.apk` to the release.
4. Long-press the APK on the release page and copy the link. It looks like:
   `https://github.com/<you>/<repo>/releases/download/v1.1.0/AiyaAnusha-debug.apk`

   *The repository must be public for the TV to download this link without signing in.*
   *For a private repository, use option C.*

**Option B: the Actions artifact (for checking a build)**

1. Repository → **Actions** → the latest green **Build LCARS APK** run.
2. Scroll down to **Artifacts** and download `AiyaAnusha-debug-apk`. GitHub delivers it as a ZIP.
3. Unzip it to get the `.apk`. Artifacts require a GitHub sign-in, so this route is better on a computer than on the TV.

---

## 2. Let the TV box install apps (one time)

On the Xiaomi box (Google TV / Android TV 11):

1. Install **Downloader by AFTVnews** from the Google Play Store on the TV (search "Downloader").
2. Go to **Settings → System → About** and press OK on **Android TV OS build** 7 times
   until it says *"You are now a developer"*.
3. Go to **Settings → Apps → Security & restrictions → Unknown sources**
   and switch it **on for Downloader only**.

   The menu names vary slightly between firmware versions. If you can't find it, Downloader
   asks for this permission itself the first time you install an APK.

---

## 3. Install

1. Open **Downloader** on the TV.
2. Type the release link from step 1 into the URL box.
   - Typing on the TV is tedious. Use the **Google TV app on your iPhone** (App Store, free):
     tap the remote icon, then the keyboard icon, and type or paste the link from the iPhone.
   - Alternatively, shorten the link first (e.g. with a URL shortener) and type the short code.
3. Downloader fetches the APK and shows the Android installer. Choose **Install**.
4. Choose **Done** (not *Open*). Back in Downloader, you can delete the downloaded APK file.

**Alternative without typing:** install **Send Files to TV** on the TV and on your iPhone.
Save the APK to the iPhone's Files app, send it to the TV, and open it there.

---

## 4. Start it

- **Google TV home:** *Apps* tab → scroll to *Your apps* → **Space Station Aiya Anusha**.
  You can also pin it to the favourites row: highlight it, hold OK, and choose *Add to favourites*.
- It shows up there because the app declares a `LEANBACK_LAUNCHER` entry with a TV banner.

### Remote control

| Remote button | What it does |
|---|---|
| D-pad | Move the focus (the focused item turns white-edged and brighter) |
| OK | Activate. Selecting a section moves the focus into its content |
| Hold OK on an app | Pin or unpin it as a favourite |
| Back | Section → Main Bridge; on the Bridge, exits the app. During Red Alert, stands down |
| Home | Returns to your normal Google TV home screen |

Your iPhone's Google TV app works as this remote too.

---

## 5. Updating

Install the newer APK the same way. It installs over the old one and keeps your settings,
because every CI build is signed with the same committed debug key (`app/debug.keystore`).

## 6. Uninstalling

**Settings → Apps → Space Station Aiya Anusha → Uninstall.** Nothing else on the box is changed by this app.

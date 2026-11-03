package at.vulkanspirit.lcarstv.data

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val group: AppGroup,
    /** True when the app declares a TV (LEANBACK_LAUNCHER) entry; false for phone-only apps. */
    val isTvApp: Boolean,
    val banner: ImageBitmap?,
    val icon: ImageBitmap?,
    /** The artwork's dominant colour (ARGB), used for the top-shelf glow. */
    val accent: Int?,
    /** True when the app accepts ACTION_SEARCH, so content can be searched inside it. */
    val searchable: Boolean,
)

/** Discovers launchable apps through PackageManager; nothing is hard-coded. */
class AppRepository(private val context: Context) {

    suspend fun loadApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        fun query(category: String): List<ResolveInfo> =
            runCatching { pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0) }
                .getOrDefault(emptyList())

        val searchable = runCatching { pm.queryIntentActivities(Intent(Intent.ACTION_SEARCH), 0) }
            .getOrDefault(emptyList())
            .mapNotNull { it.activityInfo?.takeIf { a -> a.exported }?.packageName }
            .toSet()

        val result = LinkedHashMap<String, InstalledApp>()
        fun add(ri: ResolveInfo, tv: Boolean) {
            val ai = ri.activityInfo ?: return
            if (ai.packageName == context.packageName || ai.packageName in result) return
            val label = ri.loadLabel(pm)?.toString()?.trim().orEmpty().ifEmpty { ai.packageName }
            val bannerBmp = runCatching { ai.loadBanner(pm) }.getOrNull()?.toBitmapOrNull(480, 270)
            val iconBmp = runCatching { ri.loadIcon(pm) }.getOrNull()?.toBitmapOrNull(160, 160)
            result[ai.packageName] = InstalledApp(
                packageName = ai.packageName,
                activityName = ai.name,
                label = label,
                group = AppCategorizer.classify(ai.packageName, label, ai.applicationInfo.category),
                isTvApp = tv,
                banner = bannerBmp?.asImageBitmap(),
                icon = iconBmp?.asImageBitmap(),
                accent = (bannerBmp ?: iconBmp)?.let(::accentOf),
                searchable = ai.packageName in searchable,
            )
        }

        query(Intent.CATEGORY_LEANBACK_LAUNCHER).forEach { add(it, true) }
        query(Intent.CATEGORY_LAUNCHER).forEach { add(it, false) }
        result.values.sortedBy { it.label.lowercase() }
    }

    /** Prefers the app's TV entry point, then its phone entry, then the resolved activity. */
    fun launch(launcher: Context, app: InstalledApp): Boolean {
        val pm = launcher.packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
            ?: pm.getLaunchIntentForPackage(app.packageName)
            ?: Intent(Intent.ACTION_MAIN).setClassName(app.packageName, app.activityName)
                .addCategory(Intent.CATEGORY_LAUNCHER)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return try {
            launcher.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    /** Opens the app's own search with [query] (e.g. "Star Trek" inside a streaming app). */
    fun searchIn(launcher: Context, pkg: String, query: String): Boolean = start(
        launcher,
        Intent(Intent.ACTION_SEARCH).setPackage(pkg).putExtra(SearchManager.QUERY, query),
    )

    /** True when the TV offers a system-wide search (Google TV / Assistant). */
    fun hasGlobalSearch(): Boolean = globalSearchIntents("").any { canResolve(it) }

    /** System-wide search across every app that feeds Google TV. */
    fun searchEverywhere(launcher: Context, query: String): Boolean =
        globalSearchIntents(query).any { canResolve(it) && start(launcher, it) }

    fun hasPlayStore(): Boolean = canResolve(playStoreIntent("x"))

    fun searchPlayStore(launcher: Context, query: String): Boolean = start(launcher, playStoreIntent(query))

    fun openAppInfo(launcher: Context, pkg: String): Boolean = start(
        launcher,
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkg, null)),
    )

    private fun globalSearchIntents(query: String) = listOf(
        Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH).putExtra(SearchManager.QUERY, query),
        Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query),
    )

    private fun playStoreIntent(query: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(query)}&c=apps"))

    private fun canResolve(intent: Intent): Boolean =
        runCatching { context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null }
            .getOrDefault(false)

    private fun start(launcher: Context, intent: Intent): Boolean = try {
        launcher.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }

    private fun Drawable.toBitmapOrNull(w: Int, h: Int): Bitmap? = runCatching { toBitmap(w, h) }.getOrNull()

    private fun accentOf(bitmap: Bitmap): Int? = runCatching {
        val small = Bitmap.createScaledBitmap(bitmap, 16, 9, true)
        val px = IntArray(small.width * small.height)
        small.getPixels(px, 0, small.width, 0, 0, small.width, small.height)
        ColorMath.dominant(px)
    }.getOrNull()
}

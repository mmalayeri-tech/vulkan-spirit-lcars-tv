package at.vulkanspirit.lcarstv.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
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
)

/** Discovers launchable apps through PackageManager; nothing is hard-coded. */
class AppRepository(private val context: Context) {

    suspend fun loadApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        fun query(category: String): List<ResolveInfo> =
            runCatching { pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0) }
                .getOrDefault(emptyList())

        val result = LinkedHashMap<String, InstalledApp>()
        fun add(ri: ResolveInfo, tv: Boolean) {
            val ai = ri.activityInfo ?: return
            if (ai.packageName == context.packageName || ai.packageName in result) return
            val label = ri.loadLabel(pm)?.toString()?.trim().orEmpty().ifEmpty { ai.packageName }
            val banner = runCatching { ai.loadBanner(pm) }.getOrNull()?.toImage(320, 180)
            val icon = runCatching { ri.loadIcon(pm) }.getOrNull()?.toImage(128, 128)
            result[ai.packageName] = InstalledApp(
                packageName = ai.packageName,
                activityName = ai.name,
                label = label,
                group = AppCategorizer.classify(ai.packageName, label, ai.applicationInfo.category),
                isTvApp = tv,
                banner = banner,
                icon = icon,
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

    private fun Drawable.toImage(w: Int, h: Int): ImageBitmap? =
        runCatching { toBitmap(w, h).asImageBitmap() }.getOrNull()
}

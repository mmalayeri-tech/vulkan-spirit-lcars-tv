package at.vulkanspirit.lcarstv.data

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.os.storage.StorageManager
import android.view.Display

data class StorageStat(val label: String, val total: Long, val free: Long)

data class SystemSnapshot(
    val manufacturer: String = Build.MANUFACTURER.orEmpty(),
    val model: String = Build.MODEL.orEmpty(),
    val device: String = Build.DEVICE.orEmpty(),
    val product: String = Build.PRODUCT.orEmpty(),
    val hardware: String = Build.HARDWARE.orEmpty(),
    val androidVersion: String = Build.VERSION.RELEASE.orEmpty(),
    val sdkInt: Int = Build.VERSION.SDK_INT,
    val securityPatch: String = Build.VERSION.SECURITY_PATCH.orEmpty(),
    val buildId: String = Build.DISPLAY.orEmpty(),
    val kernel: String = System.getProperty("os.version").orEmpty(),
    val abis: String = Build.SUPPORTED_ABIS.joinToString(", "),
    val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
    val memTotal: Long = 0,
    val memAvail: Long = 0,
    val lowMemory: Boolean = false,
    val storage: List<StorageStat> = emptyList(),
    val connected: Boolean = false,
    val validated: Boolean = false,
    val networkType: String = "OFFLINE",
    val ipAddresses: List<String> = emptyList(),
    val wifiLinkMbps: Int? = null,
    val wifiSignal: Int? = null,
    val display: String = "",
    val supportedModes: List<String> = emptyList(),
    val hdr: List<String> = emptyList(),
    val uptimeMs: Long = 0,
    val defaultHome: String = "",
    val isDefaultHome: Boolean = false,
    val homeAliasEnabled: Boolean = false,
) {
    /** The exact model string the user reported. Matching is informational only. */
    val isReportedTargetModel: Boolean get() = model.equals(TARGET_MODEL, ignoreCase = true)

    companion object {
        const val TARGET_MODEL = "MiTV-AKRR0"
    }
}

/** Reads device state through public, permission-light APIs. Every call is defensive. */
class SystemInfoProvider(private val context: Context) {
    private val homeAlias = ComponentName(context, "${context.packageName}.HomeAlias")

    fun snapshot(): SystemSnapshot {
        var s = SystemSnapshot(uptimeMs = SystemClock.elapsedRealtime())
        runCatching {
            val am = context.getSystemService(ActivityManager::class.java)
            val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
            s = s.copy(memTotal = mi.totalMem, memAvail = mi.availMem, lowMemory = mi.lowMemory)
        }
        s = s.copy(storage = storage())
        runCatching { s = network(s) }
        runCatching { s = display(s) }
        runCatching {
            val home = context.packageManager.resolveActivity(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), PackageManager.MATCH_DEFAULT_ONLY,
            )?.activityInfo?.packageName.orEmpty()
            s = s.copy(
                defaultHome = home.ifEmpty { "SYSTEM CHOOSER" },
                isDefaultHome = home == context.packageName,
                homeAliasEnabled = isHomeAliasEnabled(),
            )
        }
        return s
    }

    fun isHomeAliasEnabled(): Boolean =
        context.packageManager.getComponentEnabledSetting(homeAlias) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    /** Only toggles this app's own alias component. The system launcher is never touched. */
    fun setHomeAliasEnabled(enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            homeAlias,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun storage(): List<StorageStat> {
        val list = mutableListOf<StorageStat>()
        runCatching {
            val fs = StatFs(Environment.getDataDirectory().path)
            list += StorageStat("INTERNAL", fs.totalBytes, fs.availableBytes)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                val sm = context.getSystemService(StorageManager::class.java)
                sm.storageVolumes.filter { it.isRemovable }.forEach { vol ->
                    val dir = vol.directory ?: return@forEach
                    val fs = StatFs(dir.path)
                    list += StorageStat(vol.getDescription(context).uppercase(), fs.totalBytes, fs.availableBytes)
                }
            }
        }
        return list
    }

    @Suppress("DEPRECATION")
    private fun network(s: SystemSnapshot): SystemSnapshot {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val net = cm.activeNetwork ?: return s.copy(connected = false, networkType = "OFFLINE")
        val caps = cm.getNetworkCapabilities(net)
        val type = when {
            caps == null -> "UNKNOWN"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WI-FI"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "OTHER"
        }
        val ips = cm.getLinkProperties(net)?.linkAddresses
            ?.mapNotNull { it.address?.hostAddress }
            ?.filterNot { it.startsWith("fe80") }
            ?.take(3).orEmpty()
        var linkMbps: Int? = null
        var signal: Int? = null
        if (type == "WI-FI") {
            runCatching {
                val wm = context.applicationContext.getSystemService(WifiManager::class.java)
                val info = wm.connectionInfo
                linkMbps = info.linkSpeed.takeIf { it > 0 }
                signal = WifiManager.calculateSignalLevel(info.rssi, 5)
            }
        }
        return s.copy(
            connected = true,
            validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true,
            networkType = type,
            ipAddresses = ips,
            wifiLinkMbps = linkMbps,
            wifiSignal = signal,
        )
    }

    private fun display(s: SystemSnapshot): SystemSnapshot {
        val dm = context.getSystemService(DisplayManager::class.java)
        val d = dm.getDisplay(Display.DEFAULT_DISPLAY) ?: return s
        val mode = d.mode
        val modes = d.supportedModes
            .map { "${it.physicalWidth}x${it.physicalHeight}@${it.refreshRate.toInt()}" }
            .distinct()
            .sortedByDescending { it.substringBefore('x').toIntOrNull() ?: 0 }
        val hdr = d.hdrCapabilities?.supportedHdrTypes?.map {
            when (it) {
                Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "DOLBY VISION"
                Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
                Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
                Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
                else -> "TYPE $it"
            }
        }.orEmpty()
        return s.copy(
            display = "${mode.physicalWidth}x${mode.physicalHeight} @ ${"%.0f".format(mode.refreshRate)} HZ",
            supportedModes = modes,
            hdr = hdr,
        )
    }
}

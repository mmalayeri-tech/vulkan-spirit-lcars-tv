package at.vulkanspirit.lcarstv.ui.screens

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import at.vulkanspirit.lcarstv.BuildConfig
import at.vulkanspirit.lcarstv.LcarsViewModel
import at.vulkanspirit.lcarstv.data.Format
import at.vulkanspirit.lcarstv.data.SystemSnapshot
import at.vulkanspirit.lcarstv.ui.components.InfoLine
import at.vulkanspirit.lcarstv.ui.components.LcarsButton
import at.vulkanspirit.lcarstv.ui.components.LcarsPanel
import at.vulkanspirit.lcarstv.ui.components.SegmentGauge
import at.vulkanspirit.lcarstv.ui.theme.LText
import at.vulkanspirit.lcarstv.ui.theme.LocalPalette
import kotlinx.coroutines.delay

@Composable
fun EngineeringScreen(vm: LcarsViewModel) {
    val s by vm.system.collectAsStateWithLifecycle()
    val palette = LocalPalette.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            vm.refreshSystem()
            delay(2000)
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LcarsPanel("POWER & MEMORY", palette.primary, Modifier.weight(1f)) {
                val usedMem = s.memTotal - s.memAvail
                InfoLine("MEMORY IN USE", "${Format.bytes(usedMem)} / ${Format.bytes(s.memTotal)}")
                SegmentGauge(if (s.memTotal > 0) usedMem.toFloat() / s.memTotal else 0f, palette.primary)
                if (s.lowMemory) LText("LOW MEMORY CONDITION", size = 14.sp, color = palette.alert)
                Spacer(Modifier.height(4.dp))
                s.storage.forEach { st ->
                    InfoLine("${st.label} STORAGE", "${Format.bytes(st.total - st.free)} / ${Format.bytes(st.total)}")
                    SegmentGauge(if (st.total > 0) (st.total - st.free).toFloat() / st.total else 0f, palette.secondary)
                }
                if (s.storage.isEmpty()) InfoLine("STORAGE", "NOT ACCESSIBLE")
                InfoLine("CPU CORES", s.cpuCores.toString())
                InfoLine("UPTIME", Format.duration(s.uptimeMs))
            }
            LcarsPanel("SUBSPACE NETWORK", palette.tertiary, Modifier.weight(1f)) {
                InfoLine(
                    "LINK",
                    if (s.connected) s.networkType else "OFFLINE",
                    if (s.connected) Color(0xFF66CC66) else palette.alert,
                )
                InfoLine("INTERNET VALIDATED", if (s.validated) "YES" else "NO")
                s.ipAddresses.forEachIndexed { i, ip -> InfoLine(if (i == 0) "ADDRESS" else "", ip) }
                s.wifiLinkMbps?.let { InfoLine("WI-FI LINK RATE", "$it MBPS") }
                s.wifiSignal?.let {
                    InfoLine("WI-FI SIGNAL", "${it + 1} / 5")
                    SegmentGauge((it + 1) / 5f, palette.tertiary, segments = 5, warnAbove = 2f)
                }
                Spacer(Modifier.height(4.dp))
                InfoLine("DISPLAY MODE", s.display.ifEmpty { "UNKNOWN" })
                InfoLine("TOP MODE", s.supportedModes.firstOrNull() ?: "—")
                InfoLine("HDR", s.hdr.joinToString(", ").ifEmpty { "NONE REPORTED" })
            }
            LcarsPanel("HARDWARE", palette.secondary, Modifier.weight(1f)) {
                InfoLine("MAKER", s.manufacturer.uppercase())
                InfoLine(
                    "MODEL",
                    s.model,
                    if (s.isReportedTargetModel) Color(0xFF66CC66) else palette.text,
                )
                InfoLine("DEVICE / PRODUCT", "${s.device} / ${s.product}")
                InfoLine("PLATFORM", s.hardware)
                InfoLine("ANDROID", "${s.androidVersion} (API ${s.sdkInt})")
                InfoLine("SECURITY PATCH", s.securityPatch)
                InfoLine("ABI", s.abis.substringBefore(','))
                InfoLine("KERNEL", s.kernel.substringBefore('-'))
                InfoLine("HOME APP", s.defaultHome.substringAfterLast('.').uppercase())
                InfoLine("LCARS BUILD", BuildConfig.VERSION_NAME)
                LText(
                    if (s.isReportedTargetModel) "TARGET VESSEL ${SystemSnapshot.TARGET_MODEL} CONFIRMED"
                    else "TARGET WAS ${SystemSnapshot.TARGET_MODEL}; RUNNING ON ${s.model.uppercase()}",
                    size = 13.sp,
                    color = palette.textDim,
                    maxLines = 2,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LcarsButton("RUN DIAGNOSTIC", palette.primary, { vm.refreshSystem(); vm.showToast("DIAGNOSTIC COMPLETE") }, Modifier.width(170.dp), height = 36.dp, textSize = 15)
            LcarsButton("NETWORK SETTINGS", palette.tertiary, { vm.openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS) }, Modifier.width(180.dp), height = 36.dp, textSize = 15)
            LcarsButton("STORAGE SETTINGS", palette.secondary, { vm.openSystemSettings(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }, Modifier.width(180.dp), height = 36.dp, textSize = 15)
            LcarsButton("DEVICE INFO", palette.accent, { vm.openSystemSettings(context, Settings.ACTION_DEVICE_INFO_SETTINGS) }, Modifier.width(140.dp), height = 36.dp, textSize = 15)
        }
    }
}

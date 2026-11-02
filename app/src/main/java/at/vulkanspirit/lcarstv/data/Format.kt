package at.vulkanspirit.lcarstv.data

import java.util.Locale

object Format {
    fun bytes(value: Long): String {
        if (value < 1024) return "$value B"
        val units = listOf("KB", "MB", "GB", "TB")
        var v = value.toDouble() / 1024
        var i = 0
        while (v >= 1024 && i < units.lastIndex) {
            v /= 1024
            i++
        }
        return String.format(Locale.US, if (v >= 100) "%.0f %s" else "%.1f %s", v, units[i])
    }

    fun percent(used: Long, total: Long): Int =
        if (total <= 0) 0 else ((used.toDouble() / total) * 100).toInt().coerceIn(0, 100)

    fun duration(ms: Long): String {
        val totalMinutes = ms / 60_000
        val days = totalMinutes / (60 * 24)
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return if (days > 0) "${days}D ${hours}H ${minutes}M" else "${hours}H ${minutes}M"
    }
}

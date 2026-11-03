package at.vulkanspirit.lcarstv.data

import kotlin.math.max
import kotlin.math.min

/** Pure colour helpers (ARGB ints), kept free of Android types so they are unit-testable. */
object ColorMath {
    /**
     * A representative "brand" colour for an app's artwork: the average of its pixels weighted
     * by saturation × brightness, so a logo's vivid colour wins over black or white backgrounds.
     * Returns null for fully transparent or completely grey artwork.
     */
    fun dominant(pixels: IntArray): Int? {
        var r = 0.0
        var g = 0.0
        var b = 0.0
        var total = 0.0
        for (p in pixels) {
            val a = (p ushr 24) and 0xFF
            if (a < 128) continue
            val pr = (p shr 16) and 0xFF
            val pg = (p shr 8) and 0xFF
            val pb = p and 0xFF
            val mx = max(pr, max(pg, pb))
            val mn = min(pr, min(pg, pb))
            if (mx == 0) continue
            val saturation = (mx - mn).toDouble() / mx
            val weight = saturation * saturation * (mx / 255.0)
            r += pr * weight
            g += pg * weight
            b += pb * weight
            total += weight
        }
        if (total < 0.5) return null
        return (0xFF shl 24) or ((r / total).toInt() shl 16) or ((g / total).toInt() shl 8) or (b / total).toInt()
    }
}

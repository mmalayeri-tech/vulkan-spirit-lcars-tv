package at.vulkanspirit.lcarstv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class WeatherReport(
    val location: String,
    val temperatureC: Double,
    val humidity: Int,
    val windKmh: Double,
    val code: Int,
    val maxC: Double,
    val minC: Double,
    val sunrise: String,
    val sunset: String,
) {
    val description: String get() = WeatherCodes.describe(code)
}

sealed interface WeatherState {
    data object Disabled : WeatherState
    data object Loading : WeatherState
    data class Ready(val report: WeatherReport) : WeatherState
    data class Failed(val reason: String) : WeatherState
}

/** WMO weather interpretation codes as used by Open-Meteo. */
object WeatherCodes {
    fun describe(code: Int): String = when (code) {
        0 -> "CLEAR SKY"
        1 -> "MAINLY CLEAR"
        2 -> "PARTLY CLOUDY"
        3 -> "OVERCAST"
        45, 48 -> "FOG"
        51, 53, 55 -> "DRIZZLE"
        56, 57 -> "FREEZING DRIZZLE"
        61, 63, 65 -> "RAIN"
        66, 67 -> "FREEZING RAIN"
        71, 73, 75 -> "SNOWFALL"
        77 -> "SNOW GRAINS"
        80, 81, 82 -> "RAIN SHOWERS"
        85, 86 -> "SNOW SHOWERS"
        95 -> "THUNDERSTORM"
        96, 99 -> "THUNDERSTORM WITH HAIL"
        else -> "UNKNOWN CONDITIONS"
    }
}

/** Open-Meteo: free, no API key. Only the chosen preset's coordinates are sent. */
class WeatherRepository {
    suspend fun fetch(preset: GeoPreset): WeatherState = withContext(Dispatchers.IO) {
        val url = String.format(
            Locale.US,
            "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
                "&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset&timezone=auto&forecast_days=1",
            preset.latitude, preset.longitude,
        )
        try {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
            }
            val body = try {
                if (conn.responseCode != 200) return@withContext WeatherState.Failed("SENSOR ERROR ${conn.responseCode}")
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }
            val json = JSONObject(body)
            val cur = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            WeatherState.Ready(
                WeatherReport(
                    location = preset.name,
                    temperatureC = cur.getDouble("temperature_2m"),
                    humidity = cur.optInt("relative_humidity_2m"),
                    windKmh = cur.optDouble("wind_speed_10m"),
                    code = cur.optInt("weather_code"),
                    maxC = daily.getJSONArray("temperature_2m_max").getDouble(0),
                    minC = daily.getJSONArray("temperature_2m_min").getDouble(0),
                    sunrise = daily.getJSONArray("sunrise").getString(0).substringAfter('T'),
                    sunset = daily.getJSONArray("sunset").getString(0).substringAfter('T'),
                ),
            )
        } catch (e: Exception) {
            WeatherState.Failed("LONG-RANGE SENSORS OFFLINE")
        }
    }
}

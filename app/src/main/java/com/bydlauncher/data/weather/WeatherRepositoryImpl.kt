package com.bydlauncher.data.weather

import com.bydlauncher.domain.weather.WeatherInfo
import com.bydlauncher.domain.weather.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code" +
                    "&timezone=auto"

                val request = Request.Builder().url(url).build()
                val body = client.newCall(request).execute().use { resp ->
                    check(resp.isSuccessful) { "HTTP ${resp.code}" }
                    resp.body?.string() ?: error("빈 응답")
                }

                val current = JSONObject(body).getJSONObject("current")
                val weatherCode = current.getInt("weather_code")

                WeatherInfo(
                    tempCelsius = current.getDouble("temperature_2m"),
                    feelsLikeCelsius = current.getDouble("apparent_temperature"),
                    humidity = current.getInt("relative_humidity_2m"),
                    weatherCode = weatherCode,
                    description = wmoDescription(weatherCode),
                )
            }
        }

    private fun wmoDescription(code: Int): String = when (code) {
        0 -> "맑음"
        1 -> "대체로 맑음"
        2 -> "부분적 흐림"
        3 -> "흐림"
        45, 48 -> "안개"
        51, 53, 55 -> "이슬비"
        56, 57 -> "언 이슬비"
        61, 63, 65 -> "비"
        66, 67 -> "언 비"
        71, 73, 75 -> "눈"
        77 -> "눈 알갱이"
        80, 81, 82 -> "소나기"
        85, 86 -> "눈 소나기"
        95 -> "뇌우"
        96, 99 -> "우박 동반 뇌우"
        else -> "날씨 정보 없음"
    }
}

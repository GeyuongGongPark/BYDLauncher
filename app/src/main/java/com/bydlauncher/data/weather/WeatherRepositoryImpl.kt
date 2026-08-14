package com.bydlauncher.data.weather

import com.bydlauncher.BuildConfig
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
                val apiKey = BuildConfig.WEATHER_API_KEY
                val url = "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=$lat&lon=$lon&appid=$apiKey&units=metric&lang=kr"

                val request = Request.Builder().url(url).build()
                val body = client.newCall(request).execute().use { resp ->
                    check(resp.isSuccessful) { "HTTP ${resp.code}" }
                    resp.body?.string() ?: error("빈 응답")
                }

                val json = JSONObject(body)
                val main = json.getJSONObject("main")
                val weather = json.getJSONArray("weather").getJSONObject(0)

                WeatherInfo(
                    tempCelsius = main.getDouble("temp"),
                    feelsLikeCelsius = main.getDouble("feels_like"),
                    description = weather.getString("description"),
                    iconCode = weather.getString("icon"),
                    cityName = json.getString("name"),
                    humidity = main.getInt("humidity"),
                )
            }
        }
}

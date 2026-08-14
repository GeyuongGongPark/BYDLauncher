package com.bydlauncher.domain.weather

data class WeatherInfo(
    val tempCelsius: Double,
    val feelsLikeCelsius: Double,
    val description: String,
    val iconCode: String,       // OpenWeatherMap icon code (e.g. "01d")
    val cityName: String,
    val humidity: Int,          // %
)

sealed class WeatherState {
    data object Loading : WeatherState()
    data class Success(val info: WeatherInfo) : WeatherState()
    data class Error(val message: String) : WeatherState()
    data object PermissionDenied : WeatherState()
    data object LocationUnavailable : WeatherState()
}

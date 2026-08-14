package com.bydlauncher.ui.sidepanel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.domain.calendar.CalendarEvent
import com.bydlauncher.domain.calendar.CalendarRepository
import com.bydlauncher.domain.weather.WeatherRepository
import com.bydlauncher.domain.weather.WeatherState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SidePanelViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherRepository: WeatherRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        loadCalendar()
        refreshWeather()
    }

    fun refreshWeather() {
        if (!hasLocationPermission()) {
            _weatherState.value = WeatherState.PermissionDenied
            return
        }
        _weatherState.value = WeatherState.Loading
        fetchLocation { location ->
            if (location == null) {
                _weatherState.value = WeatherState.LocationUnavailable
            } else {
                fetchWeather(location.latitude, location.longitude)
            }
        }
    }

    fun onPermissionGranted() {
        refreshWeather()
        loadCalendar()
    }

    private fun loadCalendar() {
        if (!hasCalendarPermission()) return
        viewModelScope.launch {
            calendarRepository.getTodayEvents().collect { events ->
                _calendarEvents.value = events
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun hasCalendarPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    private fun fetchLocation(onResult: (Location?) -> Unit) {
        // 마지막으로 알려진 위치 먼저 시도
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        val cached = providers
            .filter { locationManager.isProviderEnabled(it) }
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        if (cached != null) {
            onResult(cached)
            return
        }

        // 캐시 없으면 단발성 위치 요청
        val enabledProvider = providers.firstOrNull { locationManager.isProviderEnabled(it) }
        if (enabledProvider == null) {
            onResult(null)
            return
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                onResult(location)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }

        runCatching {
            locationManager.requestLocationUpdates(enabledProvider, 0L, 0f, listener)
        }.onFailure { onResult(null) }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = weatherRepository.getWeather(lat, lon)
            _weatherState.value = result.fold(
                onSuccess = { WeatherState.Success(it) },
                onFailure = { WeatherState.Error(it.message ?: "알 수 없는 오류") },
            )
        }
    }
}

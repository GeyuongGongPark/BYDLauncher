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

    // 정리를 위해 현재 활성 리스너 추적
    private var activeLocationListener: LocationListener? = null

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
        loadCalendar()
        refreshWeather()
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
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        // 기존 리스너 해제 후 새로 등록
        activeLocationListener?.let { locationManager.removeUpdates(it) }
        activeLocationListener = null

        val enabledProvider = providers.firstOrNull { locationManager.isProviderEnabled(it) }

        // 캐시된 위치로 즉시 갱신
        val cached = providers
            .filter { locationManager.isProviderEnabled(it) }
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        if (cached != null) onResult(cached)

        if (enabledProvider == null) {
            if (cached == null) onResult(null)
            return
        }

        // 5km 이동마다 날씨 자동 갱신 (최소 10분 간격)
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                fetchWeather(location.latitude, location.longitude)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        activeLocationListener = listener

        runCatching {
            locationManager.requestLocationUpdates(
                enabledProvider,
                10 * 60 * 1000L,  // 최소 10분
                5_000f,            // 최소 5km
                listener,
            )
        }.onFailure {
            activeLocationListener = null
            if (cached == null) onResult(null)
        }
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

    override fun onCleared() {
        super.onCleared()
        activeLocationListener?.let { locationManager.removeUpdates(it) }
        activeLocationListener = null
    }
}

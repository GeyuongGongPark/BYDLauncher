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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var locationTimeoutJob: Job? = null

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
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )

        // 기존 리스너 및 타임아웃 해제
        activeLocationListener?.let { locationManager.removeUpdates(it) }
        activeLocationListener = null
        locationTimeoutJob?.cancel()
        locationTimeoutJob = null

        val enabledProvider = providers.firstOrNull { locationManager.isProviderEnabled(it) }

        // 캐시된 위치로 즉시 갱신
        val cached = providers
            .filter { locationManager.isProviderEnabled(it) }
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        // 중복 콜백 방지
        var delivered = false
        fun deliver(loc: Location?) {
            if (!delivered) { delivered = true; onResult(loc) }
        }

        if (cached != null) deliver(cached)

        if (enabledProvider == null) {
            deliver(null)
            return
        }

        var firstFix = cached == null  // 캐시 없으면 첫 위치 대기 중

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationTimeoutJob?.cancel()
                locationTimeoutJob = null
                if (firstFix) {
                    firstFix = false
                    deliver(location)
                    locationManager.removeUpdates(this)
                    runCatching {
                        locationManager.requestLocationUpdates(
                            enabledProvider,
                            30 * 60 * 1000L,  // 30분
                            0f,
                            this,
                        )
                    }
                } else {
                    fetchWeather(location.latitude, location.longitude)
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
        }
        activeLocationListener = listener

        runCatching {
            locationManager.requestLocationUpdates(
                enabledProvider,
                0L,
                0f,
                listener,
            )
        }.onFailure {
            activeLocationListener = null
            deliver(null)
            return
        }

        // 10초 내 위치 못 받으면 LocationUnavailable로 전환
        if (firstFix) {
            locationTimeoutJob = viewModelScope.launch {
                delay(10_000L)
                activeLocationListener?.let { locationManager.removeUpdates(it) }
                activeLocationListener = null
                deliver(null)
            }
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
        locationTimeoutJob?.cancel()
        activeLocationListener?.let { locationManager.removeUpdates(it) }
        activeLocationListener = null
    }
}

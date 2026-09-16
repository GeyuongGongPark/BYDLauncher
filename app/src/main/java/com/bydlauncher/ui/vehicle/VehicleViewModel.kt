package com.bydlauncher.ui.vehicle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.camping.sdk.ChargingReader
import com.bydlauncher.domain.vehicle.VehicleStatus
import com.bydlauncher.vehicle.sdk.BodyworkReader
import com.bydlauncher.vehicle.sdk.MalfunctionReader
import com.bydlauncher.vehicle.sdk.PM25Reader
import com.bydlauncher.vehicle.sdk.SpeedReader
import com.bydlauncher.vehicle.sdk.StatisticReader
import com.bydlauncher.vehicle.sdk.TyreReader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _status = MutableStateFlow(VehicleStatus())
    val status: StateFlow<VehicleStatus> = _status.asStateFlow()

    private val statistic = StatisticReader(context)
    private val speed = SpeedReader(context)
    private val bodywork = BodyworkReader(context)
    private val tyre = TyreReader(context)
    private val malfunction = MalfunctionReader(context)
    private val pm25 = PM25Reader(context)
    private val charging = ChargingReader(context)

    private var fastPollJob: Job? = null
    private var slowPollJob: Job? = null

    init {
        connectAll()
        startPolling()
    }

    private fun connectAll() {
        viewModelScope.launch(Dispatchers.IO) {
            statistic.connect()
            speed.connect()
            bodywork.connect()
            tyre.connect()
            malfunction.connect()
            pm25.connect()
            charging.connect()
            // 초기값 즉시 갱신
            updateFast()
            updateSlow()
        }
    }

    private fun startPolling() {
        // 5초: 속도 + 도어/윈도우 (주행 감지 빠르게)
        fastPollJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5_000L)
                updateFast()
            }
        }
        // 30초: 배터리, 충전, 타이어, 고장, 공기질
        slowPollJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(30_000L)
                updateSlow()
            }
        }
    }

    private fun updateFast() {
        val current = _status.value
        _status.value = current.copy(
            speedKmh = speed.getCurrentSpeedKmh(),
            openDoors = bodywork.getOpenDoors(),
        )
    }

    private fun updateSlow() {
        val current = _status.value
        _status.value = current.copy(
            batteryPct = statistic.getBatteryPct(),
            elecRangeKm = statistic.getElecRangeKm(),
            fuelPct = statistic.getFuelPct(),
            isCharging = charging.isConnected(),
            chargingPowerKw = charging.getChargingPowerKw(),
            chargingRestMinutes = charging.getRestMinutes(),
            tyreAlerts = tyre.getTyreAlerts(),
            malfunctions = malfunction.getMalfunctions(),
            pm25Indoor = pm25.getIndoorValue(),
            pm25Outdoor = pm25.getOutdoorValue(),
            pm25Level = pm25.getIndoorLevel(),
        )
    }

    override fun onCleared() {
        super.onCleared()
        fastPollJob?.cancel()
        slowPollJob?.cancel()
    }
}

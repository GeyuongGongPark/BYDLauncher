package com.bydlauncher.ui.vehicle

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.camping.sdk.AcController
import com.bydlauncher.domain.vehicle.VehicleControlState
import com.bydlauncher.vehicle.sdk.AcReader
import com.bydlauncher.vehicle.sdk.BodyworkReader
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
class VehicleControlViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(VehicleControlState())
    val state: StateFlow<VehicleControlState> = _state.asStateFlow()

    private val acReader = AcReader(context)
    private val acController = AcController(context)
    private val bodywork = BodyworkReader(context)

    private var pollJob: Job? = null

    companion object {
        private const val TAG = "VehicleControlViewModel"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            var available = false
            runCatching {
                acReader.connect()
                acController.connect()
                bodywork.connect()
                available = acReader.isConnected()
            }.onFailure { Log.w(TAG, "SDK 연결 실패: ${it.message}") }

            _state.value = _state.value.copy(sdkAvailable = available)
            if (available) {
                refresh()
                startPolling()
            }
        }
    }

    fun toggleAc() {
        viewModelScope.launch(Dispatchers.IO) {
            val isOn = acReader.getAcOn()
            if (isOn) acController.stop() else acController.start()
            delay(300)
            refresh()
        }
    }

    fun tempUp() = changeTemp(+1)
    fun tempDown() = changeTemp(-1)

    private fun changeTemp(delta: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = acReader.getSetTemp()
            val next = if (current in 16..32) (current + delta).coerceIn(16, 32) else 22
            acController.setTemperature(next)
            delay(300)
            refresh()
        }
    }

    private fun startPolling() {
        pollJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5_000L)
                refresh()
            }
        }
    }

    private fun refresh() {
        _state.value = VehicleControlState(
            acOn = acReader.getAcOn(),
            acTemp = acReader.getSetTemp(),
            acWindLevel = acReader.getWindLevel(),
            windowPercents = bodywork.getWindowPercents(),
            sdkAvailable = acReader.isConnected(),
        )
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}

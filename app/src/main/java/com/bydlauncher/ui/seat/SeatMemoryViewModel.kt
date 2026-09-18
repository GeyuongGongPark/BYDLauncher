package com.bydlauncher.ui.seat

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.domain.seat.SeatPreset
import com.bydlauncher.ui.vehicle.VehicleViewModel
import com.bydlauncher.vehicle.sdk.SeatController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.seatDataStore by preferencesDataStore(name = "seat_memory")

@HiltViewModel
class SeatMemoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vehicleViewModel: VehicleViewModel,
) : ViewModel() {

    private val seatController = SeatController(context)

    private val _preset = MutableStateFlow(SeatPreset())
    val preset: StateFlow<SeatPreset> = _preset.asStateFlow()

    private val _autoMode = MutableStateFlow(false)
    val autoMode: StateFlow<Boolean> = _autoMode.asStateFlow()

    private val _seatState = MutableStateFlow(SeatSdkState.LOADING)
    val seatState: StateFlow<SeatSdkState> = _seatState.asStateFlow()

    /** 저장 작업 중 여부 (UI 피드백용) */
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var lastGear = "UNKNOWN"
    private var sessionRestored = false

    enum class SeatSdkState {
        LOADING,       // 초기화 중
        AVAILABLE,     // SDK + setter 모두 사용 가능 → 실제 제어 가능
        READ_ONLY,     // SDK는 있으나 setter 미발견
        UNAVAILABLE,   // SDK 클래스 자체 없음
    }

    companion object {
        private val FORE_AFT_KEY = intPreferencesKey("seat_fore_aft")
        private val HAS_PRESET_KEY = booleanPreferencesKey("seat_has_preset")
        private val AUTO_MODE_KEY = booleanPreferencesKey("seat_auto_mode")
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadSettings()
            seatController.connect()
            _seatState.value = when {
                seatController.isAvailable -> SeatSdkState.AVAILABLE
                seatController.isClassFound -> SeatSdkState.READ_ONLY
                else -> SeatSdkState.UNAVAILABLE
            }
        }
        viewModelScope.launch {
            vehicleViewModel.status.collect { status ->
                handleGearChange(lastGear, status.currentGear)
                lastGear = status.currentGear
            }
        }
    }

    /**
     * 기어 변화 처리.
     * - P 진입: 하차 편의를 위해 시트 최대 뒤로
     * - P→D 첫 진입: 저장된 드라이빙 포지션 복원
     * 안전: 시트 이동은 P기어 상태에서만 실행 (D 복원도 직전이 P였으므로 OK)
     */
    private fun handleGearChange(old: String, new: String) {
        if (!_autoMode.value || _seatState.value != SeatSdkState.AVAILABLE) return

        if (new == "PARK" && old != "PARK") {
            // 하차 준비: 시트 최대 뒤로 (하차 후 시동 꺼질 것으로 간주)
            sessionRestored = false
            viewModelScope.launch(Dispatchers.IO) {
                seatController.moveToEntryPosition()
            }
        }

        if (new == "DRIVE" && old == "PARK" && !sessionRestored) {
            // 운전 시작: 저장된 포지션으로 복원
            sessionRestored = true
            val p = _preset.value
            if (p.hasPreset) {
                viewModelScope.launch(Dispatchers.IO) {
                    seatController.restorePosition(p.foreAft)
                }
            }
        }
    }

    /**
     * 현재 시트 포지션을 드라이빙 포지션으로 저장.
     * SDK가 없으면 사용자가 직접 입력한 값(-1 = 미입력)을 저장.
     */
    fun saveDrivingPosition() {
        _isSaving.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val foreAft = seatController.getCurrentForeAft()
            val preset = SeatPreset(foreAft = foreAft, hasPreset = foreAft >= 0)
            _preset.value = preset
            context.seatDataStore.edit {
                it[FORE_AFT_KEY] = foreAft
                it[HAS_PRESET_KEY] = foreAft >= 0
            }
            _isSaving.value = false
        }
    }

    fun toggleAutoMode() {
        val new = !_autoMode.value
        _autoMode.value = new
        viewModelScope.launch(Dispatchers.IO) {
            context.seatDataStore.edit { it[AUTO_MODE_KEY] = new }
        }
    }

    private suspend fun loadSettings() {
        val prefs = context.seatDataStore.data.first()
        _autoMode.value = prefs[AUTO_MODE_KEY] ?: false
        val foreAft = prefs[FORE_AFT_KEY] ?: -1
        val hasPreset = prefs[HAS_PRESET_KEY] ?: false
        _preset.value = SeatPreset(foreAft = foreAft, hasPreset = hasPreset)
    }
}

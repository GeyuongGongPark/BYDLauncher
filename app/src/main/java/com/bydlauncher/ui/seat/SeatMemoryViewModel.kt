package com.bydlauncher.ui.seat

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    /** SDK 초기화 완료 여부 */
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    /** SDK 사용 가능 여부 (씨라이언 7 플러스) */
    val isAvailable: Boolean get() = seatController.isAvailable

    /** 슬롯 작업 진행 중 여부 (버튼 비활성화용) */
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private var lastGear = "UNKNOWN"

    companion object {
        private val HAS_DRIVING_KEY = booleanPreferencesKey("seat_has_driving")
        private val HAS_ENTRY_KEY = booleanPreferencesKey("seat_has_entry")
        private val AUTO_MODE_KEY = booleanPreferencesKey("seat_auto_mode")

        const val DRIVING_SLOT = 1
        const val ENTRY_SLOT = 2
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            loadSettings()
            seatController.connect()
            _ready.value = true
        }
        viewModelScope.launch {
            vehicleViewModel.status.collect { status ->
                handleGearChange(lastGear, status.currentGear)
                lastGear = status.currentGear
            }
        }
    }

    /**
     * 기어 변화에 따른 자동 시트 제어.
     * - P 진입: 하차 편의 포지션(슬롯 2) 복원
     * - P→D: 드라이빙 포지션(슬롯 1) 복원
     * 안전: 시트 이동은 P기어 상태에서만 실행 (D 복원도 바로 직전 P였으므로 OK)
     */
    private fun handleGearChange(old: String, new: String) {
        if (!_autoMode.value || !seatController.isAvailable) return

        if (new == "PARK" && old != "PARK" && _preset.value.hasEntrySlot) {
            viewModelScope.launch(Dispatchers.IO) {
                seatController.recallSlot(ENTRY_SLOT)
            }
        }

        if (new == "DRIVE" && old == "PARK" && _preset.value.hasDrivingSlot) {
            viewModelScope.launch(Dispatchers.IO) {
                seatController.recallSlot(DRIVING_SLOT)
            }
        }
    }

    /** 현재 시트 포지션을 드라이빙 슬롯(1)에 저장 */
    fun saveDrivingSlot() = saveSlot(DRIVING_SLOT)

    /** 현재 시트 포지션을 하차 슬롯(2)에 저장 */
    fun saveEntrySlot() = saveSlot(ENTRY_SLOT)

    /** 드라이빙 슬롯(1)으로 수동 복원 */
    fun recallDrivingSlot() = recallSlot(DRIVING_SLOT)

    /** 하차 슬롯(2)으로 수동 복원 */
    fun recallEntrySlot() = recallSlot(ENTRY_SLOT)

    fun toggleAutoMode() {
        val new = !_autoMode.value
        _autoMode.value = new
        viewModelScope.launch(Dispatchers.IO) {
            context.seatDataStore.edit { it[AUTO_MODE_KEY] = new }
        }
    }

    private fun saveSlot(slot: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            val ok = seatController.saveSlot(slot)
            if (ok) {
                val newPreset = when (slot) {
                    DRIVING_SLOT -> _preset.value.copy(hasDrivingSlot = true)
                    ENTRY_SLOT -> _preset.value.copy(hasEntrySlot = true)
                    else -> _preset.value
                }
                _preset.value = newPreset
                context.seatDataStore.edit {
                    it[HAS_DRIVING_KEY] = newPreset.hasDrivingSlot
                    it[HAS_ENTRY_KEY] = newPreset.hasEntrySlot
                }
            }
            _busy.value = false
        }
    }

    private fun recallSlot(slot: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            seatController.recallSlot(slot)
            _busy.value = false
        }
    }

    private suspend fun loadSettings() {
        val prefs = context.seatDataStore.data.first()
        _autoMode.value = prefs[AUTO_MODE_KEY] ?: false
        _preset.value = SeatPreset(
            hasDrivingSlot = prefs[HAS_DRIVING_KEY] ?: false,
            hasEntrySlot = prefs[HAS_ENTRY_KEY] ?: false,
        )
    }
}

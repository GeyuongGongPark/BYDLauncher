package com.bydlauncher.ui.drive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.camping.sdk.GearReader
import com.bydlauncher.domain.drive.DriveEvent
import com.bydlauncher.domain.drive.DriveEventDetector
import com.bydlauncher.domain.drive.DriveEventType
import com.bydlauncher.domain.drive.DriveRepository
import com.bydlauncher.domain.drive.DriveSession
import com.bydlauncher.domain.drive.EcoScoreCalculator
import com.bydlauncher.domain.vehicle.VehicleStatusHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriveCoachUiState(
    val isSessionActive: Boolean = false,
    val sessionId: Long = -1L,
    val elapsedSeconds: Int = 0,
    val currentScore: Int = 100,
    val harshAccelCount: Int = 0,
    val harshBrakeCount: Int = 0,
    val regenCount: Int = 0,
    val lastFlashType: DriveEventType? = null,  // 화면 플래시용
    val showResultDialog: Boolean = false,
    val completedSession: DriveSession? = null,
    val recentSessions: List<DriveSession> = emptyList(),
)

@HiltViewModel
class DriveCoachViewModel @Inject constructor(
    private val driveRepository: DriveRepository,
    private val vehicleStatusHolder: VehicleStatusHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriveCoachUiState())
    val uiState: StateFlow<DriveCoachUiState> = _uiState.asStateFlow()

    private val detector = DriveEventDetector()
    private var sessionJob: Job? = null
    private var sessionStartMs = 0L
    private var lastGear = GearReader.GearState.UNKNOWN

    init {
        observeGear()
        observeRecentSessions()
    }

    private fun observeGear() {
        viewModelScope.launch {
            vehicleStatusHolder.status.collect { status ->
                val gear = try {
                    GearReader.GearState.valueOf(status.currentGear)
                } catch (_: Exception) {
                    GearReader.GearState.UNKNOWN
                }

                when {
                    gear == GearReader.GearState.DRIVE && !_uiState.value.isSessionActive -> startSession()
                    gear == GearReader.GearState.PARK && _uiState.value.isSessionActive -> endSession()
                }
                lastGear = gear
            }
        }
    }

    private fun observeRecentSessions() {
        viewModelScope.launch {
            driveRepository.getRecentSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(recentSessions = sessions)
            }
        }
    }

    private fun startSession() {
        sessionStartMs = System.currentTimeMillis()
        // UI를 즉시 활성 상태로 전환 (Room insert 완료 전에도 반응하도록)
        _uiState.value = DriveCoachUiState(
            isSessionActive = true,
            sessionId = -1L,
            recentSessions = _uiState.value.recentSessions,
        )
        viewModelScope.launch(Dispatchers.IO) {
            val id = driveRepository.startSession(sessionStartMs)
            _uiState.value = _uiState.value.copy(sessionId = id)
            startPolling(id)
        }
    }

    private fun endSession() {
        val state = _uiState.value
        if (!state.isSessionActive) return
        sessionJob?.cancel()
        val endMs = System.currentTimeMillis()
        val durationSec = ((endMs - sessionStartMs) / 1000).toInt()
        val score = EcoScoreCalculator.calculate(
            state.harshAccelCount, state.harshBrakeCount, state.regenCount
        )
        viewModelScope.launch(Dispatchers.IO) {
            driveRepository.endSession(state.sessionId, endMs, durationSec, score)
            val completed = DriveSession(
                id = state.sessionId,
                startedAt = sessionStartMs,
                endedAt = endMs,
                durationSeconds = durationSec,
                finalScore = score,
                harshAccelCount = state.harshAccelCount,
                harshBrakeCount = state.harshBrakeCount,
                regenCount = state.regenCount,
                avgInstantElecCon = 0.0,
            )
            _uiState.value = _uiState.value.copy(
                isSessionActive = false,
                showResultDialog = true,
                completedSession = completed,
            )
        }
    }

    private fun startPolling(sessionId: Long) {
        sessionJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(5_000L)
                val status = vehicleStatusHolder.status.value
                val nowMs = System.currentTimeMillis()
                val state = _uiState.value

                val events = detector.detect(
                    accelDeepness = status.accelerateDeepness.coerceAtLeast(0),
                    brakeDeepness = status.brakeDeepness.coerceAtLeast(0),
                    regenActive = status.regenActive,
                    speedKmh = status.speedKmh,
                    nowMs = nowMs,
                )

                var harshAccel = state.harshAccelCount
                var harshBrake = state.harshBrakeCount
                var regen = state.regenCount
                var lastFlash: DriveEventType? = null

                for (type in events) {
                    when (type) {
                        DriveEventType.HARSH_ACCEL -> { harshAccel++; lastFlash = type }
                        DriveEventType.HARSH_BRAKE -> { harshBrake++; lastFlash = type }
                        DriveEventType.REGEN -> regen++
                    }
                    driveRepository.recordEvent(
                        DriveEvent(
                            id = 0, sessionId = sessionId, occurredAt = nowMs,
                            type = type, intensity = 80, speedKmh = status.speedKmh,
                        )
                    )
                }

                val elapsed = ((nowMs - sessionStartMs) / 1000).toInt()
                val score = EcoScoreCalculator.calculate(harshAccel, harshBrake, regen)

                _uiState.value = state.copy(
                    elapsedSeconds = elapsed,
                    currentScore = score,
                    harshAccelCount = harshAccel,
                    harshBrakeCount = harshBrake,
                    regenCount = regen,
                    lastFlashType = lastFlash,
                )
            }
        }
    }

    fun dismissResultDialog() {
        _uiState.value = _uiState.value.copy(showResultDialog = false, completedSession = null)
    }

    fun clearFlash() {
        _uiState.value = _uiState.value.copy(lastFlashType = null)
    }
}

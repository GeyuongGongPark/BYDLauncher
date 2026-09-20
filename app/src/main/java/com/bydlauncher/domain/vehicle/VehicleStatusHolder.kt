package com.bydlauncher.domain.vehicle

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VehicleViewModel이 업데이트하는 싱글톤 차량 상태 홀더.
 * DriveCoachViewModel, SeatMemoryViewModel 등이 VehicleViewModel을 직접 inject하지 않고
 * 이 홀더를 통해 상태를 구독한다.
 */
@Singleton
class VehicleStatusHolder @Inject constructor() {
    private val _status = MutableStateFlow(VehicleStatus())
    val status: StateFlow<VehicleStatus> = _status.asStateFlow()

    fun update(status: VehicleStatus) {
        _status.value = status
    }
}

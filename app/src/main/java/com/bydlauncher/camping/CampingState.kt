package com.bydlauncher.camping

sealed interface CampingState {
    /** 비활성 */
    object Idle : CampingState

    /** 차량에 BYD AC SDK 없음 (씨라이언7 등 자체 캠핑 모드 있는 차종, 또는 미지원) */
    object SdkUnavailable : CampingState

    /** 서비스 시작 중 (SDK 로딩 + 에어컨 켜는 중) */
    object Starting : CampingState

    /** 캠핑 모드 실행 중 */
    data class Running(
        val batteryPct: Int,           // -1 = 조회 불가
        val elapsedMs: Long,
        val outsideTemp: Int,          // -1 = 조회 불가
        val targetTemp: Int,
        val isCharging: Boolean = false,
        val chargingPowerKw: Double = 0.0,
        val estimatedRangeKm: Int = -1, // -1 = 조회 불가
    ) : CampingState

    /** 오류 발생 */
    data class Error(val message: String) : CampingState
}

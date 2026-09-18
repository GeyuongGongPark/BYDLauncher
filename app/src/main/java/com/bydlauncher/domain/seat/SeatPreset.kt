package com.bydlauncher.domain.seat

/**
 * 메모리 시트 슬롯 저장 여부.
 * 실제 포지션 값은 차량 SDK(BYDAutoSettingDevice)가 관리하므로 앱에서 따로 저장하지 않는다.
 *
 * slot 1 = 드라이빙 포지션 (D기어 시 자동 복원)
 * slot 2 = 하차 편의 포지션 (P기어 시 자동 복원)
 */
data class SeatPreset(
    val hasDrivingSlot: Boolean = false,   // 슬롯 1 저장 완료
    val hasEntrySlot: Boolean = false,     // 슬롯 2 저장 완료
)

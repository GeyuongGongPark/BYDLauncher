package com.bydlauncher.domain.seat

/**
 * 메모리 시트 드라이빙 포지션 프리셋.
 * foreAft: 앞뒤 포지션 0~100 (0=최대 앞, 100=최대 뒤).
 * hasPreset: 저장된 프리셋이 있는지 여부.
 */
data class SeatPreset(
    val foreAft: Int = -1,
    val hasPreset: Boolean = false,
)

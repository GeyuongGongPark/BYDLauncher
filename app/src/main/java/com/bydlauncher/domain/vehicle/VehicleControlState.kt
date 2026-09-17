package com.bydlauncher.domain.vehicle

/**
 * AC 퀵 컨트롤 + 창문 개방률 상태.
 * sdkAvailable=false이면 UI 전체 비활성.
 */
data class VehicleControlState(
    val acOn: Boolean = false,
    val acTemp: Int = -1,                           // 설정 온도 °C, -1=조회 불가
    val acWindLevel: Int = -1,                      // 풍량 1~7, -1=조회 불가
    val windowPercents: Map<Int, Int> = emptyMap(), // area 1~4 → 개방률 %
    val sdkAvailable: Boolean = false,
)

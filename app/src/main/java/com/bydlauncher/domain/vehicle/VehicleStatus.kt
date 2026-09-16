package com.bydlauncher.domain.vehicle

data class VehicleStatus(
    val batteryPct: Double = -1.0,          // EV 배터리 (0.0~100.0), -1 = 조회 불가
    val elecRangeKm: Int = -1,              // EV 주행 가능 거리 (km)
    val fuelPct: Int = -1,                  // 연료 잔량 %, HEV용 (-1 = 없음)
    val isCharging: Boolean = false,
    val chargingPowerKw: Double = 0.0,
    val chargingRestMinutes: Int = -1,      // 충전 완료까지 남은 분
    val speedKmh: Double = 0.0,
    val tyreAlerts: List<String> = emptyList(),
    val malfunctions: List<String> = emptyList(),
    val openDoors: List<String> = emptyList(),
    val pm25Indoor: Int = -1,
    val pm25Outdoor: Int = -1,
    val pm25Level: Int = 0,                 // 0=조회불가, 1=매우좋음 ~ 6=매우나쁨
) {
    val isDriving: Boolean get() = speedKmh > 1.0
    val hasAlert: Boolean get() =
        tyreAlerts.isNotEmpty() || malfunctions.isNotEmpty() || (isDriving && openDoors.isNotEmpty())
}

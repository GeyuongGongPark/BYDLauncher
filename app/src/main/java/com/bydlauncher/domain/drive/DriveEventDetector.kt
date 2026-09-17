package com.bydlauncher.domain.drive

/**
 * 급가속/급제동/회생제동 감지.
 * 같은 타입 이벤트는 쿨다운(3초) 후에만 재감지.
 */
class DriveEventDetector {

    private val lastEventMs = mutableMapOf<DriveEventType, Long>()
    private val cooldownMs = 3_000L

    /**
     * @param accelDeepness 가속 페달 깊이 0~100
     * @param brakeDeepness 브레이크 페달 깊이 0~100
     * @param regenActive   회생제동 활성 여부
     * @param speedKmh      현재 속도
     * @param nowMs         현재 시각 epoch ms
     */
    fun detect(
        accelDeepness: Int,
        brakeDeepness: Int,
        regenActive: Boolean,
        speedKmh: Double,
        nowMs: Long,
    ): List<DriveEventType> {
        val detected = mutableListOf<DriveEventType>()

        if (accelDeepness >= 80 && canFire(DriveEventType.HARSH_ACCEL, nowMs)) {
            detected.add(DriveEventType.HARSH_ACCEL)
            lastEventMs[DriveEventType.HARSH_ACCEL] = nowMs
        }
        if (brakeDeepness >= 70 && canFire(DriveEventType.HARSH_BRAKE, nowMs)) {
            detected.add(DriveEventType.HARSH_BRAKE)
            lastEventMs[DriveEventType.HARSH_BRAKE] = nowMs
        }
        if (regenActive && canFire(DriveEventType.REGEN, nowMs)) {
            detected.add(DriveEventType.REGEN)
            lastEventMs[DriveEventType.REGEN] = nowMs
        }

        return detected
    }

    private fun canFire(type: DriveEventType, nowMs: Long): Boolean {
        val last = lastEventMs[type] ?: 0L
        return nowMs - last >= cooldownMs
    }
}

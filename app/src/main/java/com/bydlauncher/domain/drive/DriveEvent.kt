package com.bydlauncher.domain.drive

enum class DriveEventType { HARSH_ACCEL, HARSH_BRAKE, REGEN }

data class DriveEvent(
    val id: Long,
    val sessionId: Long,
    val occurredAt: Long,
    val type: DriveEventType,
    val intensity: Int,
    val speedKmh: Double,
)

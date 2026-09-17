package com.bydlauncher.domain.drive

data class DriveSession(
    val id: Long,
    val startedAt: Long,
    val endedAt: Long,          // 0 = 진행 중
    val durationSeconds: Int,
    val finalScore: Int,
    val harshAccelCount: Int,
    val harshBrakeCount: Int,
    val regenCount: Int,
    val avgInstantElecCon: Double,
) {
    val isActive: Boolean get() = endedAt == 0L
}

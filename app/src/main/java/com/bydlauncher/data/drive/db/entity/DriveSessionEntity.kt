package com.bydlauncher.data.drive.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drive_sessions")
data class DriveSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,                // epoch ms
    val endedAt: Long = 0L,             // 0 = 진행 중
    val durationSeconds: Int = 0,
    val finalScore: Int = 100,
    val harshAccelCount: Int = 0,
    val harshBrakeCount: Int = 0,
    val regenCount: Int = 0,
    val avgInstantElecCon: Double = 0.0, // kWh/100km
)

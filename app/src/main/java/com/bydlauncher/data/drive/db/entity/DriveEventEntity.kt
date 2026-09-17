package com.bydlauncher.data.drive.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drive_events",
    foreignKeys = [ForeignKey(
        entity = DriveSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("sessionId")],
)
data class DriveEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val occurredAt: Long,               // epoch ms
    val type: String,                   // HARSH_ACCEL | HARSH_BRAKE | REGEN
    val intensity: Int,                 // 0~100
    val speedKmh: Double,
)

package com.bydlauncher.data.drive

import com.bydlauncher.data.drive.db.DriveDatabase
import com.bydlauncher.data.drive.db.entity.DriveEventEntity
import com.bydlauncher.data.drive.db.entity.DriveSessionEntity
import com.bydlauncher.domain.drive.DriveEvent
import com.bydlauncher.domain.drive.DriveEventType
import com.bydlauncher.domain.drive.DriveRepository
import com.bydlauncher.domain.drive.DriveSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DriveRepositoryImpl @Inject constructor(
    private val db: DriveDatabase,
) : DriveRepository {

    override suspend fun startSession(startedAt: Long): Long =
        db.sessionDao().insert(DriveSessionEntity(startedAt = startedAt))

    override suspend fun endSession(id: Long, endedAt: Long, durationSeconds: Int, finalScore: Int) {
        val existing = db.sessionDao().getById(id) ?: return
        val harshAccel = db.eventDao().countByType(id, "HARSH_ACCEL")
        val harshBrake = db.eventDao().countByType(id, "HARSH_BRAKE")
        val regen = db.eventDao().countByType(id, "REGEN")
        db.sessionDao().update(
            existing.copy(
                endedAt = endedAt,
                durationSeconds = durationSeconds,
                finalScore = finalScore,
                harshAccelCount = harshAccel,
                harshBrakeCount = harshBrake,
                regenCount = regen,
            )
        )
    }

    override suspend fun recordEvent(event: DriveEvent) {
        db.eventDao().insert(
            DriveEventEntity(
                sessionId = event.sessionId,
                occurredAt = event.occurredAt,
                type = event.type.name,
                intensity = event.intensity,
                speedKmh = event.speedKmh,
            )
        )
    }

    override fun getRecentSessions(): Flow<List<DriveSession>> =
        db.sessionDao().getRecent().map { list ->
            list.map { e ->
                DriveSession(
                    id = e.id,
                    startedAt = e.startedAt,
                    endedAt = e.endedAt,
                    durationSeconds = e.durationSeconds,
                    finalScore = e.finalScore,
                    harshAccelCount = e.harshAccelCount,
                    harshBrakeCount = e.harshBrakeCount,
                    regenCount = e.regenCount,
                    avgInstantElecCon = e.avgInstantElecCon,
                )
            }
        }
}

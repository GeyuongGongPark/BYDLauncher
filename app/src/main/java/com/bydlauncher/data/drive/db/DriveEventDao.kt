package com.bydlauncher.data.drive.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bydlauncher.data.drive.db.entity.DriveEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveEventDao {

    @Insert
    suspend fun insert(event: DriveEventEntity)

    @Query("SELECT * FROM drive_events WHERE sessionId = :sessionId ORDER BY occurredAt")
    fun getBySession(sessionId: Long): Flow<List<DriveEventEntity>>

    @Query("SELECT COUNT(*) FROM drive_events WHERE sessionId = :sessionId AND type = :type")
    suspend fun countByType(sessionId: Long, type: String): Int
}

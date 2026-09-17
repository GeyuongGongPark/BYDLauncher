package com.bydlauncher.data.drive.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bydlauncher.data.drive.db.entity.DriveSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveSessionDao {

    @Insert
    suspend fun insert(session: DriveSessionEntity): Long

    @Update
    suspend fun update(session: DriveSessionEntity)

    @Query("SELECT * FROM drive_sessions WHERE id = :id")
    suspend fun getById(id: Long): DriveSessionEntity?

    @Query("SELECT * FROM drive_sessions ORDER BY startedAt DESC LIMIT 20")
    fun getRecent(): Flow<List<DriveSessionEntity>>
}

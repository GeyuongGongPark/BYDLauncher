package com.bydlauncher.data.drive.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bydlauncher.data.drive.db.entity.DriveEventEntity
import com.bydlauncher.data.drive.db.entity.DriveSessionEntity

@Database(
    entities = [DriveSessionEntity::class, DriveEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class DriveDatabase : RoomDatabase() {
    abstract fun sessionDao(): DriveSessionDao
    abstract fun eventDao(): DriveEventDao
}

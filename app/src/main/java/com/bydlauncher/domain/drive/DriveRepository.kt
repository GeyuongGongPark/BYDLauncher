package com.bydlauncher.domain.drive

import kotlinx.coroutines.flow.Flow

interface DriveRepository {
    suspend fun startSession(startedAt: Long): Long
    suspend fun endSession(id: Long, endedAt: Long, durationSeconds: Int, finalScore: Int)
    suspend fun recordEvent(event: DriveEvent)
    fun getRecentSessions(): Flow<List<DriveSession>>
}

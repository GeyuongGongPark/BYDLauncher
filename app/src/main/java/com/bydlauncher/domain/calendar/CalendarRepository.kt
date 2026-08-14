package com.bydlauncher.domain.calendar

import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getTodayEvents(): Flow<List<CalendarEvent>>
}

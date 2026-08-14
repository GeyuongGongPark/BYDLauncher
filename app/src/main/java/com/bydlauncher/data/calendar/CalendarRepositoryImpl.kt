package com.bydlauncher.data.calendar

import android.content.Context
import android.provider.CalendarContract
import com.bydlauncher.domain.calendar.CalendarEvent
import com.bydlauncher.domain.calendar.CalendarRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Calendar
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CalendarRepository {

    override fun getTodayEvents(): Flow<List<CalendarEvent>> = flow {
        emit(queryTodayEvents())
    }.flowOn(Dispatchers.IO)

    private fun queryTodayEvents(): List<CalendarEvent> {
        val now = Calendar.getInstance()
        val startOfDay = now.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L - 1

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.DISPLAY_COLOR,
        )
        val selection = "(${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?) " +
            "OR (${CalendarContract.Events.DTSTART} <= ? AND ${CalendarContract.Events.DTEND} >= ?)"
        val selArgs = arrayOf(
            startOfDay.toString(), endOfDay.toString(),
            startOfDay.toString(), startOfDay.toString(),
        )

        val events = mutableListOf<CalendarEvent>()
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection, selection, selArgs,
            "${CalendarContract.Events.DTSTART} ASC",
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CalendarContract.Events._ID)
            val titleIdx = cursor.getColumnIndex(CalendarContract.Events.TITLE)
            val startIdx = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
            val endIdx = cursor.getColumnIndex(CalendarContract.Events.DTEND)
            val allDayIdx = cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)
            val colorIdx = cursor.getColumnIndex(CalendarContract.Events.DISPLAY_COLOR)
            while (cursor.moveToNext()) {
                events += CalendarEvent(
                    id = cursor.getLong(idIdx),
                    title = cursor.getString(titleIdx) ?: "",
                    startMillis = cursor.getLong(startIdx),
                    endMillis = cursor.getLong(endIdx),
                    allDay = cursor.getInt(allDayIdx) == 1,
                    color = if (colorIdx >= 0) cursor.getInt(colorIdx) else 0xFF4285F4.toInt(),
                )
            }
        }
        return events.take(5)
    }
}

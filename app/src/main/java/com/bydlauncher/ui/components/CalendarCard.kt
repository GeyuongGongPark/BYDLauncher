package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.calendar.CalendarEvent
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarCard(
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "오늘 일정",
            fontSize = 11.sp,
            color = TextSecondary,
            letterSpacing = 0.5.sp,
        )
        Spacer(Modifier.height(8.dp))

        if (events.isEmpty()) {
            Text(
                text = "일정 없음",
                fontSize = 13.sp,
                color = TextDisabled,
            )
        } else {
            events.forEach { event ->
                EventRow(event)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent) {
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeText = if (event.allDay) "종일" else timeFmt.format(Date(event.startMillis))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(event.color)),
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = event.title,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = timeText,
                fontSize = 11.sp,
                color = TextDisabled,
            )
        }
    }
}

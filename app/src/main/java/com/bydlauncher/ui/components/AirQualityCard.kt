package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.vehicle.VehicleStatus
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.TextSecondary

@Composable
fun AirQualityCard(
    status: VehicleStatus,
    modifier: Modifier = Modifier,
) {
    // PM2.5 데이터 없으면 숨김
    if (status.pm25Indoor < 0 && status.pm25Outdoor < 0) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = "공기질", fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (status.pm25Indoor >= 0) {
                AirQualityItem(
                    label = "실내",
                    value = status.pm25Indoor,
                    level = status.pm25Level,
                    modifier = Modifier.weight(1f),
                )
            }
            if (status.pm25Outdoor >= 0) {
                AirQualityItem(
                    label = "실외",
                    value = status.pm25Outdoor,
                    level = 0, // 실외 등급은 별도 조회 필요 — 수치만 표시
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AirQualityItem(
    label: String,
    value: Int,
    level: Int,
    modifier: Modifier = Modifier,
) {
    val color = pm25Color(level, value)
    val levelText = pm25LevelText(level, value)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(color)
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$value", fontSize = 14.sp, color = color)
                Spacer(Modifier.width(2.dp))
                Text("μg/m³", fontSize = 9.sp, color = TextSecondary)
            }
            Text(levelText, fontSize = 9.sp, color = color)
        }
    }
}

private fun pm25Color(level: Int, value: Int): Color {
    val effectiveLevel = if (level in 1..6) level else when {
        value < 0  -> 0
        value < 15 -> 1
        value < 35 -> 2
        value < 75 -> 3
        value < 115 -> 4
        value < 150 -> 5
        else -> 6
    }
    return when (effectiveLevel) {
        1 -> Color(0xFF66BB6A)
        2 -> Color(0xFF66BB6A)
        3 -> Color(0xFFFFB74D)
        4 -> Color(0xFFFF7043)
        5 -> Color(0xFFEF5350)
        6 -> Color(0xFFAB47BC)
        else -> Color(0xFF90A4AE)
    }
}

private fun pm25LevelText(level: Int, value: Int): String {
    val effectiveLevel = if (level in 1..6) level else when {
        value < 0  -> 0
        value < 15 -> 1
        value < 35 -> 2
        value < 75 -> 3
        value < 115 -> 4
        value < 150 -> 5
        else -> 6
    }
    return when (effectiveLevel) {
        1 -> "매우 좋음"
        2 -> "좋음"
        3 -> "보통"
        4 -> "나쁨"
        5 -> "매우 나쁨"
        6 -> "위험"
        else -> ""
    }
}

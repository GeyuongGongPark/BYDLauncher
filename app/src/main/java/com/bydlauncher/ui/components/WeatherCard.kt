package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.weather.WeatherInfo
import com.bydlauncher.domain.weather.WeatherState
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun WeatherCard(
    state: WeatherState,
    onRetry: () -> Unit,
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
            text = "날씨",
            fontSize = 11.sp,
            color = TextSecondary,
            letterSpacing = 0.5.sp,
        )
        Spacer(Modifier.height(8.dp))

        when (state) {
            WeatherState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = AccentCyan,
                )
            }

            is WeatherState.Success -> WeatherContent(state.info)

            WeatherState.PermissionDenied -> StatusRow(
                icon = Icons.Default.LocationOff,
                message = "위치 권한 필요",
                actionLabel = null,
                onAction = null,
            )

            WeatherState.LocationUnavailable -> StatusRow(
                icon = Icons.Default.LocationOff,
                message = "위치 확인 불가",
                actionLabel = "다시 시도",
                onAction = onRetry,
            )

            is WeatherState.Error -> StatusRow(
                icon = Icons.Default.Refresh,
                message = "날씨 로드 실패",
                actionLabel = "다시 시도",
                onAction = onRetry,
            )
        }
    }
}

@Composable
private fun WeatherContent(info: WeatherInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = "${info.tempCelsius.roundToInt()}°",
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                color = TextPrimary,
            )
            Text(
                text = info.description,
                fontSize = 12.sp,
                color = TextSecondary,
            )
            Text(
                text = info.cityName,
                fontSize = 11.sp,
                color = TextDisabled,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "습도",
                    tint = AccentCyan,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(text = "${info.humidity}%", fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = "체감",
                    tint = AccentCyan,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "체감 ${info.feelsLikeCelsius.roundToInt()}°",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextDisabled,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(text = message, fontSize = 12.sp, color = TextDisabled)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = actionLabel,
                fontSize = 12.sp,
                color = AccentCyan,
                modifier = Modifier.clickable { onAction() },
            )
        }
    }
}

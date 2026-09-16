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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.vehicle.VehicleStatus
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun VehicleStatusCard(
    status: VehicleStatus,
    modifier: Modifier = Modifier,
) {
    // 배터리 정보가 없으면 카드 숨김
    if (status.batteryPct < 0 && !status.isCharging) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = "차량", fontSize = 11.sp, color = TextSecondary, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))

        // EV 배터리
        if (status.batteryPct >= 0) {
            BatteryRow(
                label = "배터리",
                pct = status.batteryPct / 100.0,
                valueText = "${status.batteryPct.roundToInt()}%",
                icon = if (status.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                barColor = batteryColor(status.batteryPct),
            )
            if (status.elecRangeKm >= 0) {
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text("주행 가능 ${status.elecRangeKm}km", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }

        // 연료 (HEV)
        if (status.fuelPct in 0..100) {
            Spacer(Modifier.height(6.dp))
            BatteryRow(
                label = "연료",
                pct = status.fuelPct / 100.0,
                valueText = "${status.fuelPct}%",
                icon = Icons.Default.LocalGasStation,
                barColor = if (status.fuelPct < 20) Color(0xFFEF5350) else AccentCyan,
            )
        }

        // 충전 중
        if (status.isCharging) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("충전 중", fontSize = 11.sp, color = AccentCyan)
                val restText = if (status.chargingRestMinutes > 0) {
                    val h = status.chargingRestMinutes / 60
                    val m = status.chargingRestMinutes % 60
                    if (h > 0) "${h}시간 ${m}분" else "${m}분 후 완료"
                } else if (status.chargingPowerKw > 0.0) {
                    "${String.format("%.1f", status.chargingPowerKw)}kW"
                } else ""
                if (restText.isNotEmpty()) {
                    Text(restText, fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BatteryRow(
    label: String,
    pct: Double,
    valueText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    barColor: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = barColor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(32.dp))
        LinearProgressIndicator(
            progress = { pct.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.2f),
        )
        Spacer(Modifier.width(6.dp))
        Text(valueText, fontSize = 11.sp, color = TextPrimary, modifier = Modifier.width(34.dp))
    }
}

private fun batteryColor(pct: Double): Color = when {
    pct < 15.0 -> Color(0xFFEF5350)
    pct < 30.0 -> Color(0xFFFFB74D)
    else -> Color(0xFF66BB6A)
}

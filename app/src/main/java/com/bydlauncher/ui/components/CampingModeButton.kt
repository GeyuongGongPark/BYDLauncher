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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.camping.CampingState
import com.bydlauncher.ui.camping.CampingSettingsDialog
import com.bydlauncher.ui.camping.CampingViewModel
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.AccentCyanDim
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@Composable
fun CampingModeButton(
    modifier: Modifier = Modifier,
    viewModel: CampingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundSurface)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AcUnit,
                contentDescription = null,
                tint = when (state) {
                    is CampingState.Running -> AccentCyan
                    is CampingState.Starting -> AccentCyanDim
                    else -> TextDisabled
                },
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "캠핑 모드",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        when (val s = state) {
            is CampingState.Idle -> {
                Button(
                    onClick = { showSettings = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim),
                ) {
                    Text("시작", fontSize = 14.sp)
                }
            }

            is CampingState.SdkUnavailable -> {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(disabledContainerColor = BackgroundSurface),
                ) {
                    Text("미지원 차종", color = TextDisabled, fontSize = 13.sp)
                }
            }

            is CampingState.Starting -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AccentCyan,
                        strokeWidth = 2.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("시작 중...", color = TextSecondary, fontSize = 13.sp)
                }
            }

            is CampingState.Running -> {
                // 상태 표시
                RunningStatus(s)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.stopCamping() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                ) {
                    Text("종료", fontSize = 14.sp)
                }
            }

            is CampingState.Error -> {
                Text(
                    text = "오류: ${s.message}",
                    color = Color(0xFFEF5350),
                    fontSize = 11.sp,
                )
                Spacer(Modifier.height(6.dp))
                Button(
                    onClick = { viewModel.startCamping() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim),
                ) {
                    Text("재시도", fontSize = 13.sp)
                }
            }
        }
    }

    if (showSettings) {
        CampingSettingsDialog(
            prefs = viewModel.prefs,
            onDismiss = { showSettings = false },
            onConfirm = {
                showSettings = false
                viewModel.startCamping()
            },
        )
    }
}

@Composable
private fun RunningStatus(s: CampingState.Running) {
    val elapsed = s.elapsedMs
    val h = TimeUnit.MILLISECONDS.toHours(elapsed)
    val m = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60
    val timeStr = if (h > 0) "${h}시간 ${m}분" else "${m}분"

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("목표 온도", color = TextSecondary, fontSize = 11.sp)
            Text("${s.targetTemp}°C", color = TextPrimary, fontSize = 11.sp)
        }
        if (s.batteryPct >= 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("배터리", color = TextSecondary, fontSize = 11.sp)
                Text("${s.batteryPct}%", color = AccentCyan, fontSize = 11.sp)
            }
        }
        if (s.outsideTemp >= 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("외부 온도", color = TextSecondary, fontSize = 11.sp)
                Text("${s.outsideTemp}°C", color = TextPrimary, fontSize = 11.sp)
            }
        }
        if (s.isCharging) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("충전 중", color = TextSecondary, fontSize = 11.sp)
                }
                Text(
                    text = if (s.chargingPowerKw > 0.0) "${String.format("%.1f", s.chargingPowerKw)}kW" else "—",
                    color = AccentCyan,
                    fontSize = 11.sp,
                )
            }
        }
        if (s.estimatedRangeKm >= 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("주행 가능", color = TextSecondary, fontSize = 11.sp)
                Text("${s.estimatedRangeKm}km", color = TextPrimary, fontSize = 11.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("경과", color = TextSecondary, fontSize = 11.sp)
            Text(timeStr, color = TextPrimary, fontSize = 11.sp)
        }
    }
}

package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.AccentCyanDim
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import com.bydlauncher.ui.vehicle.VehicleControlViewModel

@Composable
fun VehicleControlCard(
    modifier: Modifier = Modifier,
    viewModel: VehicleControlViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundSurface)
            .padding(12.dp),
    ) {
        // 헤더
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AcUnit,
                contentDescription = null,
                tint = if (state.acOn) AccentCyan else TextDisabled,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("차량 제어", color = TextSecondary, fontSize = 12.sp)
        }

        if (!state.sdkAvailable) {
            Spacer(Modifier.height(6.dp))
            Text("미지원 차종", color = TextDisabled, fontSize = 11.sp)
            return@Column
        }

        Spacer(Modifier.height(8.dp))

        // AC ON/OFF 스위치
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (state.acOn) "에어컨 ON" else "에어컨 OFF",
                color = if (state.acOn) TextPrimary else TextSecondary,
                fontSize = 13.sp,
            )
            Switch(
                checked = state.acOn,
                onCheckedChange = { viewModel.toggleAc() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentCyan,
                    checkedTrackColor = AccentCyanDim,
                ),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            )
        }

        // 온도 조절 (항상 표시)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("목표 온도", color = TextSecondary, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = viewModel::tempDown,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "온도 낮추기",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = if (state.acTemp > 0) "${state.acTemp}°C" else "--°C",
                    color = TextPrimary,
                    fontSize = 13.sp,
                )
                IconButton(
                    onClick = viewModel::tempUp,
                    modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "온도 올리기",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // 풍량
        if (state.acWindLevel > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("풍량", color = TextSecondary, fontSize = 11.sp)
                Text("${state.acWindLevel}단", color = TextPrimary, fontSize = 11.sp)
            }
        }

        // 창문 개방률
        val openWindows = state.windowPercents.filter { it.value > 5 }
        if (openWindows.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            val windowNames = mapOf(1 to "운전석", 2 to "조수석", 3 to "좌후", 4 to "우후")
            openWindows.forEach { (area, pct) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${windowNames[area] ?: "창문$area"} 창", color = TextSecondary, fontSize = 11.sp)
                    Text("$pct%", color = AccentCyan, fontSize = 11.sp)
                }
            }
        }
    }
}

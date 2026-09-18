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
import androidx.compose.material.icons.filled.AirlineSeatReclineExtra
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.ui.seat.SeatMemoryViewModel
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.AccentCyanDim
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary

@Composable
fun SeatMemoryCard(
    modifier: Modifier = Modifier,
    viewModel: SeatMemoryViewModel = hiltViewModel(),
) {
    val preset by viewModel.preset.collectAsState()
    val autoMode by viewModel.autoMode.collectAsState()
    val ready by viewModel.ready.collectAsState()
    val busy by viewModel.busy.collectAsState()

    val sdkAvailable = ready && viewModel.isAvailable

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
                imageVector = Icons.Default.AirlineSeatReclineExtra,
                contentDescription = null,
                tint = if (sdkAvailable) AccentCyan else TextDisabled,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("메모리 시트", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(
                text = when {
                    !ready -> "초기화 중"
                    sdkAvailable -> "M1 / M2"
                    else -> "미지원"
                },
                color = if (sdkAvailable) AccentCyan else TextDisabled,
                fontSize = 10.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        if (!ready) {
            Text("초기화 중...", color = TextDisabled, fontSize = 11.sp)
            return@Column
        }

        if (!sdkAvailable) {
            Text(
                "씨라이언 7 플러스(DiLink 5.0) 전용 기능입니다.",
                color = TextDisabled,
                fontSize = 11.sp,
            )
            return@Column
        }

        // 슬롯 1: 드라이빙 포지션
        SlotRow(
            label = "M1  드라이빙",
            saved = preset.hasDrivingSlot,
            busy = busy,
            onSave = viewModel::saveDrivingSlot,
            onRecall = viewModel::recallDrivingSlot,
        )

        Spacer(Modifier.height(6.dp))

        // 슬롯 2: 하차 편의 포지션
        SlotRow(
            label = "M2  하차 편의",
            saved = preset.hasEntrySlot,
            busy = busy,
            onSave = viewModel::saveEntrySlot,
            onRecall = viewModel::recallEntrySlot,
        )

        Spacer(Modifier.height(8.dp))

        // 자동 제어 토글
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("자동 제어", color = TextSecondary, fontSize = 11.sp)
                Text(
                    text = if (autoMode) "P→M2, D→M1" else "수동",
                    color = TextDisabled,
                    fontSize = 10.sp,
                )
            }
            Switch(
                checked = autoMode,
                onCheckedChange = { viewModel.toggleAutoMode() },
                enabled = preset.hasDrivingSlot || preset.hasEntrySlot,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentCyan,
                    checkedTrackColor = AccentCyanDim,
                ),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            )
        }
    }
}

@Composable
private fun SlotRow(
    label: String,
    saved: Boolean,
    busy: Boolean,
    onSave: () -> Unit,
    onRecall: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (saved) TextPrimary else TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
        )
        // 저장
        TextButton(
            onClick = onSave,
            enabled = !busy,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 36.dp),
        ) {
            Icon(
                Icons.Default.BookmarkAdd,
                contentDescription = "저장",
                tint = if (busy) TextDisabled else AccentCyan,
                modifier = Modifier.size(14.dp),
            )
        }
        // 복원 (저장된 경우에만 활성화)
        TextButton(
            onClick = onRecall,
            enabled = saved && !busy,
            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 36.dp),
        ) {
            Icon(
                Icons.Default.RestorePage,
                contentDescription = "복원",
                tint = if (saved && !busy) TextPrimary else TextDisabled,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

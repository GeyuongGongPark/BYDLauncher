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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.bydlauncher.ui.seat.SeatMemoryViewModel
import com.bydlauncher.ui.seat.SeatMemoryViewModel.SeatSdkState
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
    val seatState by viewModel.seatState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

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
                tint = if (seatState == SeatSdkState.AVAILABLE) AccentCyan else TextDisabled,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("메모리 시트", color = TextSecondary, fontSize = 12.sp)

            Spacer(Modifier.weight(1f))

            // SDK 상태 뱃지
            val (badgeText, badgeColor) = when (seatState) {
                SeatSdkState.LOADING -> "초기화 중" to TextDisabled
                SeatSdkState.AVAILABLE -> "사용 가능" to AccentCyan
                SeatSdkState.READ_ONLY -> "읽기 전용" to TextSecondary
                SeatSdkState.UNAVAILABLE -> "미지원" to TextDisabled
            }
            Text(badgeText, color = badgeColor, fontSize = 10.sp)
        }

        Spacer(Modifier.height(8.dp))

        // 미지원 차종: 간단히 표시 후 종료
        if (seatState == SeatSdkState.UNAVAILABLE) {
            Text(
                "씨라이언 7 플러스(DiLink 5.0) 전용 기능입니다.",
                color = TextDisabled,
                fontSize = 11.sp,
            )
            return@Column
        }

        // 저장된 드라이빙 포지션
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("드라이빙 포지션", color = TextSecondary, fontSize = 11.sp)
            Text(
                text = if (preset.hasPreset) "앞뒤 ${100 - preset.foreAft}%" else "미설정",
                color = if (preset.hasPreset) TextPrimary else TextDisabled,
                fontSize = 11.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        // [현재 위치 저장] 버튼
        Button(
            onClick = viewModel::saveDrivingPosition,
            enabled = seatState != SeatSdkState.LOADING && !isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyanDim,
                contentColor = TextPrimary,
                disabledContainerColor = BackgroundSurface,
                disabledContentColor = TextDisabled,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 40.dp),
        ) {
            Icon(
                Icons.Default.BookmarkAdd,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (isSaving) "저장 중..." else "현재 위치 저장",
                fontSize = 12.sp,
            )
        }

        // 자동 제어 토글 (SDK AVAILABLE 상태에서만)
        if (seatState == SeatSdkState.AVAILABLE) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("자동 제어", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = if (autoMode) "P기어→뒤로, D기어→복원" else "수동 모드",
                        color = TextDisabled,
                        fontSize = 10.sp,
                    )
                }
                Switch(
                    checked = autoMode,
                    onCheckedChange = { viewModel.toggleAutoMode() },
                    enabled = preset.hasPreset,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentCyan,
                        checkedTrackColor = AccentCyanDim,
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                )
            }
        }
    }
}

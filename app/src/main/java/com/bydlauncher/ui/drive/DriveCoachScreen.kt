package com.bydlauncher.ui.drive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.domain.drive.DriveSession
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun DriveCoachScreen(
    modifier: Modifier = Modifier,
    viewModel: DriveCoachViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 현재 세션
            item {
                if (state.isSessionActive) {
                    ActiveSessionCard(state)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundCard)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("D 기어 진입 시 주행 세션 자동 시작", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // 히스토리 헤더
            if (state.recentSessions.isNotEmpty()) {
                item {
                    Text("최근 주행", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
                items(state.recentSessions.filter { !it.isActive }) { session ->
                    SessionHistoryRow(session)
                }
            }
        }

        // 결과 팝업
        if (state.showResultDialog && state.completedSession != null) {
            SessionResultDialog(
                session = state.completedSession!!,
                onDismiss = viewModel::dismissResultDialog,
            )
        }
    }
}

@Composable
private fun ActiveSessionCard(state: DriveCoachUiState) {
    val h = state.elapsedSeconds / 3600
    val m = (state.elapsedSeconds % 3600) / 60
    val s = state.elapsedSeconds % 60
    val timeStr = if (h > 0) "${h}:${m.pad()}:${s.pad()}" else "${m.pad()}:${s.pad()}"

    val scoreColor = when {
        state.currentScore >= 80 -> Color(0xFF4CAF50)
        state.currentScore >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("주행 중", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(timeStr, color = TextSecondary, fontSize = 13.sp)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "${state.currentScore}",
                color = scoreColor,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(" 점", color = scoreColor, fontSize = 22.sp, modifier = Modifier.align(Alignment.Bottom).padding(bottom = 8.dp))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatChip("급가속", "${state.harshAccelCount}회", Color(0xFFFF6F00))
            StatChip("급제동", "${state.harshBrakeCount}회", Color(0xFFD32F2F))
            StatChip("회생", "${state.regenCount}회", Color(0xFF4CAF50))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun SessionHistoryRow(session: DriveSession) {
    val dateStr = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(Date(session.startedAt))
    val durationMin = TimeUnit.SECONDS.toMinutes(session.durationSeconds.toLong())
    val scoreColor = when {
        session.finalScore >= 80 -> Color(0xFF4CAF50)
        session.finalScore >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(dateStr, color = TextPrimary, fontSize = 13.sp)
            Text("${durationMin}분", color = TextSecondary, fontSize = 11.sp)
        }
        Text("${session.finalScore}점", color = scoreColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

private fun Int.pad(): String = toString().padStart(2, '0')

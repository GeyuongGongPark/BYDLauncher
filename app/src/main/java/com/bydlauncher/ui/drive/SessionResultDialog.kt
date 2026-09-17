package com.bydlauncher.ui.drive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.drive.DriveSession
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.AccentCyanDim
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import java.util.concurrent.TimeUnit

@Composable
fun SessionResultDialog(
    session: DriveSession,
    onDismiss: () -> Unit,
) {
    val scoreColor = when {
        session.finalScore >= 80 -> Color(0xFF4CAF50)
        session.finalScore >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val h = TimeUnit.SECONDS.toHours(session.durationSeconds.toLong())
    val m = TimeUnit.SECONDS.toMinutes(session.durationSeconds.toLong()) % 60
    val durationStr = if (h > 0) "${h}시간 ${m}분" else "${m}분"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundCard,
        title = {
            Text("주행 완료", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // 에코 점수
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${session.finalScore}",
                        color = scoreColor,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(" 점", color = scoreColor, fontSize = 20.sp)
                }

                Spacer(Modifier.height(16.dp))

                StatRow("주행 시간", durationStr)
                StatRow("급가속 횟수", "${session.harshAccelCount}회")
                StatRow("급제동 횟수", "${session.harshBrakeCount}회")
                StatRow("회생제동 횟수", "${session.regenCount}회")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDim),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text("확인", color = AccentCyan)
            }
        },
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp)
    }
}

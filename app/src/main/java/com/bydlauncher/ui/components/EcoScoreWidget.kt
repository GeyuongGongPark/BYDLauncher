package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
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
import com.bydlauncher.ui.drive.DriveCoachViewModel
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import com.bydlauncher.ui.vehicle.VehicleViewModel

@Composable
fun EcoScoreWidget(
    modifier: Modifier = Modifier,
    driveViewModel: DriveCoachViewModel = hiltViewModel(),
    vehicleViewModel: VehicleViewModel = hiltViewModel(),
) {
    val driveState by driveViewModel.uiState.collectAsState()
    val vehicleStatus by vehicleViewModel.status.collectAsState()

    if (!driveState.isSessionActive) return

    val scoreColor = when {
        driveState.currentScore >= 80 -> Color(0xFF4CAF50)
        driveState.currentScore >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val modeLabel = when (vehicleStatus.operationMode) {
        0 -> "EV"
        1 -> "HEV"
        2 -> "SPORT"
        else -> ""
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 에코 점수
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                tint = scoreColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "${driveState.currentScore}",
                color = scoreColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("점", color = TextSecondary, fontSize = 9.sp)
        }

        // 순간 전비
        if (vehicleStatus.instantElecCon >= 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.ElectricCar,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = String.format("%.1f", vehicleStatus.instantElecCon),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text("kWh/100km", color = TextSecondary, fontSize = 8.sp)
            }
        }

        // 주행 모드
        if (modeLabel.isNotEmpty()) {
            Text(
                text = modeLabel,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

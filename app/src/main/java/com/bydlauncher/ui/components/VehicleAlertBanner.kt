package com.bydlauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.vehicle.VehicleStatus

@Composable
fun VehicleAlertBanner(
    status: VehicleStatus,
    modifier: Modifier = Modifier,
) {
    val alerts = buildList {
        status.malfunctions.forEach { add(AlertItem(Icons.Default.Warning, it, Color(0xFFEF5350))) }
        status.tyreAlerts.forEach { add(AlertItem(Icons.Default.TireRepair, it, Color(0xFFFFB74D))) }
        if (status.isDriving && status.openDoors.isNotEmpty()) {
            status.openDoors.forEach { add(AlertItem(Icons.Default.DoorFront, "$it 열림", Color(0xFFFFB74D))) }
        }
    }

    AnimatedVisibility(
        visible = alerts.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1200))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            alerts.forEach { alert ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = alert.icon,
                        contentDescription = null,
                        tint = alert.color,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(alert.message, fontSize = 12.sp, color = alert.color)
                }
            }
        }
    }
}

private data class AlertItem(
    val icon: ImageVector,
    val message: String,
    val color: Color,
)

package com.bydlauncher.ui.camping

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bydlauncher.camping.storage.CampingPrefs
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary

@Composable
fun CampingSettingsDialog(
    prefs: CampingPrefs,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    var temp by remember { mutableFloatStateOf(prefs.targetTemp.toFloat()) }
    var battery by remember { mutableFloatStateOf(prefs.stopBatteryPct.toFloat()) }
    var hours by remember { mutableFloatStateOf(prefs.maxHours.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(BackgroundCard)
                .padding(24.dp),
        ) {
            Text("캠핑 모드 설정", color = TextPrimary, fontSize = 18.sp)

            Spacer(Modifier.height(24.dp))

            SettingSlider(
                label = "목표 온도",
                value = temp,
                valueText = "${temp.toInt()}°C",
                range = 16f..30f,
                onValueChange = { temp = it },
            )

            Spacer(Modifier.height(16.dp))

            SettingSlider(
                label = "종료 배터리",
                value = battery,
                valueText = "${battery.toInt()}%",
                range = 10f..50f,
                onValueChange = { battery = it },
            )

            Spacer(Modifier.height(16.dp))

            SettingSlider(
                label = "최대 시간",
                value = hours,
                valueText = "${hours.toInt()}시간",
                range = 1f..12f,
                onValueChange = { hours = it },
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("취소", color = TextSecondary)
                }
                TextButton(onClick = {
                    prefs.targetTemp = temp.toInt()
                    prefs.stopBatteryPct = battery.toInt()
                    prefs.maxHours = hours.toInt()
                    onConfirm()
                }) {
                    Text("시작", color = AccentCyan)
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = TextSecondary, fontSize = 13.sp)
            Text(valueText, color = AccentCyan, fontSize = 13.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1,
            colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan),
        )
    }
}

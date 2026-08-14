package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.TextDisabled

@Composable
fun SidePanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(BackgroundCard.copy(alpha = 0.6f))
            .padding(vertical = 24.dp),
    ) {
        // 시계
        ClockWidget(showDate = true)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(24.dp))

        // 차량 상태 placeholder
        PlaceholderCard(
            label = "차량 상태",
            hint = "Phase 2",
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        // 날씨 placeholder
        PlaceholderCard(
            label = "날씨",
            hint = "위치 기반 · Phase 2",
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        // 캘린더 placeholder
        PlaceholderCard(
            label = "일정",
            hint = "캘린더 · Phase 2",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun PlaceholderCard(
    label: String,
    hint: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DividerColor.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextDisabled,
        )
        Text(
            text = hint,
            fontSize = 11.sp,
            color = TextDisabled.copy(alpha = 0.6f),
        )
    }
}

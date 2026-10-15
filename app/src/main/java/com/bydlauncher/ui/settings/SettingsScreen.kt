package com.bydlauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.domain.kbo.KboTeam
import com.bydlauncher.ui.kbo.KboViewModel
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    kboViewModel: KboViewModel = hiltViewModel(),
) {
    val selectedTeam by kboViewModel.favoriteTeam.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSurface)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = "런처 설정",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(24.dp))

        // KBO 섹션
        SettingsSectionHeader(title = "KBO 야구")
        Spacer(Modifier.height(12.dp))

        Text(
            text = "응원팀",
            color = TextSecondary,
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(10.dp))

        KboTeamGrid(
            selectedTeam = selectedTeam,
            onTeamSelected = { team ->
                if (selectedTeam == team) {
                    kboViewModel.clearTeam()
                } else {
                    kboViewModel.selectTeam(team)
                }
            },
        )

        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
        Spacer(Modifier.height(20.dp))

        // 시스템 섹션
        SettingsSectionHeader(title = "시스템")
        Spacer(Modifier.height(12.dp))

        BydSystemSettingsButton()
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = AccentCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun KboTeamGrid(
    selectedTeam: KboTeam?,
    onTeamSelected: (KboTeam) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(KboTeam.entries) { team ->
            KboTeamCell(
                team = team,
                isSelected = selectedTeam == team,
                onClick = { onTeamSelected(team) },
            )
        }
    }
}

@Composable
private fun KboTeamCell(
    team: KboTeam,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) team.color.copy(alpha = 0.15f) else BackgroundCard)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) team.color else DividerColor,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(team.color),
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = team.shortName,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun BydSystemSettingsButton() {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundCard)
            .clickable {
                for (pkg in listOf("com.byd.carsettings", "com.byd.settings", "com.android.settings")) {
                    val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: continue
                    runCatching { context.startActivity(intent) }.onSuccess { return@clickable }
                }
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "BYD 시스템 설정",
            color = TextPrimary,
            fontSize = 13.sp,
        )
    }
}

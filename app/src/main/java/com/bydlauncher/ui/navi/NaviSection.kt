package com.bydlauncher.ui.navi

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.domain.navi.NaviApp
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import com.bydlauncher.ui.utils.toImageBitmap

/**
 * Landscape HOME에 표시되는 네비게이션 섹션.
 *
 * - 미선택: 설치된 앱 선택 카드 표시
 * - 선택됨 + 임베딩 가능(시스템 앱): [EmbeddedNaviView]
 * - 선택됨 + 임베딩 불가: 탭하여 실행 fallback 카드
 */
@Composable
fun NaviSection(
    modifier: Modifier = Modifier,
    viewModel: NaviViewModel = hiltViewModel(),
) {
    val selected by viewModel.selectedNaviApp.collectAsState()
    val installed = viewModel.installedNaviApps

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "내비게이션", fontSize = 13.sp, color = TextSecondary)
            if (selected != null) {
                TextButton(onClick = { viewModel.clearNaviApp() }) {
                    Text(text = "변경", fontSize = 13.sp, color = AccentCyan)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (selected == null) {
            if (installed.isEmpty()) {
                NoNaviAppsPlaceholder()
            } else {
                NaviAppSelector(apps = installed, onSelect = { viewModel.selectNaviApp(it.packageName) })
            }
        } else {
            NaviActiveView(app = selected!!)
        }
    }
}

@Composable
private fun NaviAppSelector(apps: List<NaviApp>, onSelect: (NaviApp) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        apps.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { app ->
                    NaviAppCard(app = app, modifier = Modifier.weight(1f), onClick = { onSelect(app) })
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NaviAppCard(app: NaviApp, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    val icon: ImageBitmap? = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName).toImageBitmap()
        }.getOrNull()
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = app.displayName,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
            )
        } else {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DividerColor),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(text = app.displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
private fun NaviActiveView(app: NaviApp) {
    var embeddingFailed by remember(app.packageName) { mutableStateOf(false) }

    if (!embeddingFailed) {
        EmbeddedNaviView(
            packageName = app.packageName,
            modifier = Modifier.fillMaxSize(),
            onEmbeddingFailed = { embeddingFailed = true },
        )
    } else {
        NaviFallbackCard(app = app)
    }
}

@Composable
private fun NaviFallbackCard(app: NaviApp) {
    val context = LocalContext.current
    val icon: ImageBitmap? = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName).toImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundCard)
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp))
            .clickable {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = app.displayName,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)),
                )
                Spacer(Modifier.height(12.dp))
            }
            Text(text = app.displayName, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(text = "탭하여 실행", fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun NoNaviAppsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundCard)
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "지원 네비 앱 없음", fontSize = 15.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "카카오맵 / T맵 / 네이버지도 / 구글맵",
                fontSize = 12.sp,
                color = Color(0xFF4A5070),
            )
        }
    }
}

package com.bydlauncher.ui.components

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.navi.SUPPORTED_NAVI_APPS
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.BackgroundSurface
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary
import com.bydlauncher.ui.utils.toImageBitmap

private val MUSIC_PACKAGES = listOf(
    "com.byd.music.kr",
    "com.spotify.music",
    "com.apple.android.music",
    "skplanet.musicmate",
    "com.vivid.music.byd",
    "com.byd.mediacenter",
    "com.google.android.music",
)

data class DockItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
)

@Composable
fun AppDock(
    onOpenAppDrawer: () -> Unit,
    onOpenDriveCoach: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showMusicPicker by remember { mutableStateOf(false) }

    // 설치된 음악 앱 목록 (최초 1회 계산)
    val installedMusicApps = remember {
        MUSIC_PACKAGES.mapNotNull { pkg ->
            val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: return@mapNotNull null
            val icon: ImageBitmap? = runCatching {
                context.packageManager.getApplicationIcon(pkg).toImageBitmap()
            }.getOrNull()
            val label = runCatching {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(pkg, 0)
                ).toString()
            }.getOrDefault(pkg)
            AppEntry(pkg, label, icon)
        }
    }

    val items = listOf(
        DockItem(Icons.Default.Navigation, "지도") {
            for (navi in SUPPORTED_NAVI_APPS) {
                val intent = context.packageManager.getLaunchIntentForPackage(navi.packageName)
                    ?: continue
                if (runCatching { context.startActivity(intent) }.isSuccess) break
            }
        },
        DockItem(Icons.Default.MusicNote, "음악") {
            if (installedMusicApps.size == 1) {
                // 하나면 바로 실행
                val intent = context.packageManager.getLaunchIntentForPackage(
                    installedMusicApps[0].packageName
                )
                runCatching { intent?.let { context.startActivity(it) } }
            } else {
                showMusicPicker = true
            }
        },
        DockItem(Icons.Default.Phone, "전화") {
            runCatching { context.startActivity(Intent(Intent.ACTION_DIAL)) }
        },
        DockItem(Icons.Default.Settings, "설정") {
            for (pkg in listOf("com.byd.carsettings", "com.byd.settings", "com.android.settings")) {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg) ?: continue
                if (runCatching { context.startActivity(intent) }.isSuccess) break
            }
        },
        DockItem(Icons.Default.Eco, "드라이브") { onOpenDriveCoach() },
        DockItem(Icons.Default.Apps, "앱") { onOpenAppDrawer() },
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(BackgroundCard.copy(alpha = 0.8f))
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item -> DockButton(item) }
    }

    if (showMusicPicker) {
        MusicPickerDialog(
            apps = installedMusicApps,
            onSelect = { pkg ->
                showMusicPicker = false
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                runCatching { intent?.let { context.startActivity(it) } }
            },
            onDismiss = { showMusicPicker = false },
        )
    }
}

@Composable
private fun MusicPickerDialog(
    apps: List<AppEntry>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundSurface,
        title = { Text("음악 앱 선택", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                apps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(app.packageName) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (app.icon != null) {
                            Image(
                                bitmap = app.icon,
                                contentDescription = app.label,
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)),
                            )
                        } else {
                            Icon(
                                Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(36.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(app.label, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        },
    )
}

@Composable
private fun DockButton(item: DockItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .clickable { item.onClick() }
            .padding(8.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = AccentCyan,
            modifier = Modifier.size(28.dp),
        )
        Text(text = item.label, fontSize = 10.sp, color = TextSecondary)
    }
}

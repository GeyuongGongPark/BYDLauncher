package com.bydlauncher.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.TextSecondary

data class DockItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun AppDock(
    onOpenAppDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val items = listOf(
        DockItem(Icons.Default.Navigation, "지도") {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
                ?: Intent(Intent.ACTION_VIEW).apply { `package` = "com.google.android.apps.maps" }
            runCatching { context.startActivity(intent) }
        },
        DockItem(Icons.Default.MusicNote, "음악") {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.music")
                ?: Intent(Intent.ACTION_VIEW)
            runCatching { context.startActivity(intent) }
        },
        DockItem(Icons.Default.Phone, "전화") {
            val intent = Intent(Intent.ACTION_DIAL)
            runCatching { context.startActivity(intent) }
        },
        DockItem(Icons.Default.Settings, "설정") {
            val intent = Intent(Settings.ACTION_SETTINGS)
            runCatching { context.startActivity(intent) }
        },
        DockItem(Icons.Default.Apps, "앱") {
            onOpenAppDrawer()
        },
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
        items.forEach { item ->
            DockButton(item)
        }
    }
}

@Composable
private fun DockButton(item: DockItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
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
        Text(
            text = item.label,
            fontSize = 10.sp,
            color = TextSecondary,
        )
    }
}

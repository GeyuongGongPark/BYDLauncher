package com.bydlauncher.overlay

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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
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

@Composable
fun OverlayDock(onGoHome: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(BackgroundCard.copy(alpha = 0.92f))
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OverlayButton(icon = Icons.Default.Home, label = "홈", onClick = onGoHome)
        OverlayButton(icon = Icons.Default.MusicNote, label = "음악") {
            runCatching {
                val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.music")
                    ?: Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
        OverlayButton(icon = Icons.Default.Phone, label = "전화") {
            runCatching {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
        OverlayButton(icon = Icons.Default.Settings, label = "설정") {
            runCatching {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
private fun OverlayButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AccentCyan,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
        )
    }
}

package com.bydlauncher.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bydlauncher.domain.apps.AppInfo
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextSecondary
import com.bydlauncher.ui.utils.toImageBitmap

private const val MAX_FAVORITES = 8

@Composable
fun FavoritesGrid(
    apps: List<AppInfo>,
    isEditing: Boolean,
    onLaunch: (AppInfo) -> Unit,
    onRemove: (AppInfo) -> Unit,
    onAddSlotClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slots = apps + List((MAX_FAVORITES - apps.size).coerceAtLeast(0)) { null }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(slots, key = { it?.packageName ?: "empty_${slots.indexOf(it)}" }) { app ->
            if (app != null) {
                FilledSlot(
                    app = app,
                    isEditing = isEditing,
                    onLaunch = { onLaunch(app) },
                    onRemove = { onRemove(app) },
                    onLongPress = onLongPress,
                )
            } else if (isEditing) {
                EmptySlot(onClick = onAddSlotClick)
            } else {
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun FilledSlot(
    app: AppInfo,
    isEditing: Boolean,
    onLaunch: () -> Unit,
    onRemove: () -> Unit,
    onLongPress: () -> Unit,
) {
    Box(
        modifier = Modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isEditing) BackgroundCard else BackgroundCard.copy(alpha = 0f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { if (!isEditing) onLaunch() },
                        onLongPress = { onLongPress() },
                    )
                }
                .padding(8.dp),
        ) {
            Image(
                bitmap = remember(app.packageName) { app.icon.toImageBitmap() },
                contentDescription = app.label,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = app.label,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }

        // 편집 모드 X 버튼
        if (isEditing) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(CircleShape)
                    .background(AccentCyan)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "제거",
                    tint = com.bydlauncher.ui.theme.BackgroundDeep,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptySlot(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(DividerColor.copy(alpha = 0.4f))
            .border(1.dp, DividerColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "앱 추가",
            tint = TextDisabled,
            modifier = Modifier.size(24.dp),
        )
    }
}

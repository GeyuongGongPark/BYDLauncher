package com.bydlauncher.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.ui.components.CalendarCard
import com.bydlauncher.ui.components.ClockWidget
import com.bydlauncher.ui.components.WeatherCard
import com.bydlauncher.ui.sidepanel.SidePanelViewModel
import com.bydlauncher.ui.theme.AccentCyan
import com.bydlauncher.ui.theme.TextSecondary

@Composable
fun PortraitHomeContent(
    modifier: Modifier = Modifier,
    sidePanelViewModel: SidePanelViewModel = hiltViewModel(),
) {
    val weatherState by sidePanelViewModel.weatherState.collectAsState()
    val calendarEvents by sidePanelViewModel.calendarEvents.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        ClockWidget(showDate = true)
        Spacer(Modifier.height(24.dp))
        WeatherCard(state = weatherState, onRetry = sidePanelViewModel::refreshWeather)
        Spacer(Modifier.height(12.dp))
        CalendarCard(events = calendarEvents)
    }
}

@Composable
fun LandscapeHomeContent(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val favoriteApps by homeViewModel.favoriteApps.collectAsState()
    val nonFavoriteApps by homeViewModel.nonFavoriteApps.collectAsState()
    val isEditing by homeViewModel.isEditingFavorites.collectAsState()
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Column {
            // 상단 레이블 + 편집 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "즐겨찾기",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                TextButton(onClick = {
                    if (isEditing) homeViewModel.exitEditFavorites()
                    else homeViewModel.toggleEditFavorites()
                }) {
                    Text(
                        text = if (isEditing) "완료" else "편집",
                        fontSize = 13.sp,
                        color = AccentCyan,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            FavoritesGrid(
                apps = favoriteApps,
                isEditing = isEditing,
                onLaunch = { app ->
                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                },
                onRemove = { app -> homeViewModel.removeFavorite(app.packageName) },
                onAddSlotClick = { showPicker = true },
                onLongPress = { homeViewModel.toggleEditFavorites() },
            )
        }
    }

    if (showPicker) {
        AppPickerDialog(
            viewModel = homeViewModel,
            onDismiss = { showPicker = false },
        )
    }
}

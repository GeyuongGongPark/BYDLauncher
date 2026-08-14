package com.bydlauncher.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.domain.weather.WeatherState
import com.bydlauncher.ui.sidepanel.SidePanelViewModel
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor

@Composable
fun SidePanel(modifier: Modifier = Modifier) {
    val viewModel: SidePanelViewModel = hiltViewModel()
    val weatherState by viewModel.weatherState.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()

    // 위치 + 캘린더 권한 동시 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) viewModel.onPermissionGranted()
    }

    LaunchedEffect(weatherState) {
        if (weatherState is WeatherState.PermissionDenied) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_CALENDAR,
                )
            )
        }
    }

    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(BackgroundCard.copy(alpha = 0.6f))
            .padding(vertical = 24.dp),
    ) {
        ClockWidget(showDate = true)

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(
            color = DividerColor,
            thickness = 0.5.dp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(20.dp))

        WeatherCard(
            state = weatherState,
            onRetry = viewModel::refreshWeather,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        CalendarCard(
            events = calendarEvents,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

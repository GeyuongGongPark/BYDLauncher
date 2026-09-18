package com.bydlauncher.ui.components

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.MainActivity
import com.bydlauncher.domain.weather.WeatherState
import com.bydlauncher.ui.sidepanel.SidePanelViewModel
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.ThemeMode
import com.bydlauncher.ui.theme.TextSecondary
import com.bydlauncher.ui.vehicle.VehicleViewModel

@Composable
fun SidePanel(modifier: Modifier = Modifier) {
    val viewModel: SidePanelViewModel = hiltViewModel()
    val vehicleViewModel: VehicleViewModel = hiltViewModel()
    val weatherState by viewModel.weatherState.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val vehicleStatus by vehicleViewModel.status.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val activity = LocalContext.current as ComponentActivity
    var isDefaultHome by remember { mutableStateOf(MainActivity.isDefaultHome(activity)) }

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
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
    ) {
        // 클럭 + 테마 토글
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ClockWidget(showDate = true, modifier = Modifier.weight(1f))
            IconButton(
                onClick = viewModel::toggleTheme,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = if (themeMode == ThemeMode.DARKER) Icons.Default.Brightness4 else Icons.Default.Brightness2,
                    contentDescription = "테마 전환",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

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

        Spacer(Modifier.height(12.dp))

        VehicleStatusCard(
            status = vehicleStatus,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        AirQualityCard(
            status = vehicleStatus,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(12.dp))

        UpdateBanner(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(12.dp))

        VehicleControlCard(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(12.dp))

        SeatMemoryCard(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(Modifier.height(8.dp))

        CampingModeButton(modifier = Modifier.padding(horizontal = 16.dp))

        if (!isDefaultHome) {
            Spacer(Modifier.height(8.dp))
            SetDefaultHomeButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick = {
                    MainActivity.openDefaultHomeSettings(activity)
                    isDefaultHome = MainActivity.isDefaultHome(activity)
                },
            )
        }
    }
}

package com.bydlauncher.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.ui.apps.AppDrawer
import com.bydlauncher.ui.components.AppDock
import com.bydlauncher.ui.components.DriveFlashOverlay
import com.bydlauncher.ui.components.EcoScoreWidget
import com.bydlauncher.ui.components.SidePanel
import com.bydlauncher.ui.components.StatusBar
import com.bydlauncher.ui.components.VehicleAlertBanner
import com.bydlauncher.ui.drive.DriveCoachScreen
import com.bydlauncher.ui.drive.DriveCoachViewModel
import com.bydlauncher.ui.theme.BackgroundDeep
import com.bydlauncher.ui.vehicle.VehicleViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            currentTab = currentTab,
            onToggleAppDrawer = viewModel::toggleAppDrawer,
            onToggleDriveCoach = viewModel::toggleDriveCoach,
        )
    } else {
        PortraitLayout(
            currentTab = currentTab,
            onToggleAppDrawer = viewModel::toggleAppDrawer,
            onToggleDriveCoach = viewModel::toggleDriveCoach,
        )
    }
}

@Composable
private fun LandscapeLayout(
    currentTab: HomeTab,
    onToggleAppDrawer: () -> Unit,
    onToggleDriveCoach: () -> Unit,
    vehicleViewModel: VehicleViewModel = hiltViewModel(),
    driveViewModel: DriveCoachViewModel = hiltViewModel(),
) {
    val vehicleStatus by vehicleViewModel.status.collectAsState()
    val driveState by driveViewModel.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        SidePanel()

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                VehicleAlertBanner(status = vehicleStatus)
                AnimatedContent(
                    targetState = currentTab,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        slideInHorizontally(tween(220)) { it * direction } togetherWith
                            slideOutHorizontally(tween(220)) { -it * direction }
                    },
                    label = "tab",
                ) { tab ->
                    when (tab) {
                        HomeTab.HOME -> LandscapeHomeContent()
                        HomeTab.APP_DRAWER -> AppDrawer()
                        HomeTab.DRIVE_COACH -> DriveCoachScreen()
                    }
                }
                AppDock(
                    onOpenAppDrawer = onToggleAppDrawer,
                    onOpenDriveCoach = onToggleDriveCoach,
                )
            }

            // 에코 위젯 (세션 중에만 표시)
            if (driveState.isSessionActive) {
                EcoScoreWidget(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp),
                )
            }

            // 플래시 오버레이
            DriveFlashOverlay(
                flashType = driveState.lastFlashType,
                onFlashDone = driveViewModel::clearFlash,
            )
        }
    }
}

@Composable
private fun PortraitLayout(
    currentTab: HomeTab,
    onToggleAppDrawer: () -> Unit,
    onToggleDriveCoach: () -> Unit,
    vehicleViewModel: VehicleViewModel = hiltViewModel(),
    driveViewModel: DriveCoachViewModel = hiltViewModel(),
) {
    val vehicleStatus by vehicleViewModel.status.collectAsState()
    val driveState by driveViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StatusBar()
            VehicleAlertBanner(status = vehicleStatus)

            AnimatedContent(
                targetState = currentTab,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    slideInHorizontally(tween(220)) { it * direction } togetherWith
                        slideOutHorizontally(tween(220)) { -it * direction }
                },
                label = "tab",
            ) { tab ->
                when (tab) {
                    HomeTab.HOME -> PortraitHomeContent()
                    HomeTab.APP_DRAWER -> AppDrawer()
                    HomeTab.DRIVE_COACH -> DriveCoachScreen()
                }
            }

            AppDock(
                onOpenAppDrawer = onToggleAppDrawer,
                onOpenDriveCoach = onToggleDriveCoach,
            )
        }

        DriveFlashOverlay(
            flashType = driveState.lastFlashType,
            onFlashDone = driveViewModel::clearFlash,
        )
    }
}

package com.bydlauncher.ui.home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.ui.apps.AppDrawer
import com.bydlauncher.ui.components.AppDock
import com.bydlauncher.ui.components.SidePanel
import com.bydlauncher.ui.components.StatusBar
import com.bydlauncher.ui.theme.BackgroundDeep

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            currentTab = currentTab,
            onToggleAppDrawer = viewModel::toggleAppDrawer,
        )
    } else {
        PortraitLayout(
            currentTab = currentTab,
            onToggleAppDrawer = viewModel::toggleAppDrawer,
        )
    }
}

@Composable
private fun LandscapeLayout(
    currentTab: HomeTab,
    onToggleAppDrawer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        SidePanel()

        Column(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = currentTab,
                modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab",
            ) { tab ->
                when (tab) {
                    HomeTab.HOME -> LandscapeHomeContent()
                    HomeTab.APP_DRAWER -> AppDrawer()
                }
            }
            AppDock(onOpenAppDrawer = onToggleAppDrawer)
        }
    }
}

@Composable
private fun PortraitLayout(
    currentTab: HomeTab,
    onToggleAppDrawer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        StatusBar()

        AnimatedContent(
            targetState = currentTab,
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab",
        ) { tab ->
            when (tab) {
                HomeTab.HOME -> PortraitHomeContent()
                HomeTab.APP_DRAWER -> AppDrawer()
            }
        }

        AppDock(onOpenAppDrawer = onToggleAppDrawer)
    }
}

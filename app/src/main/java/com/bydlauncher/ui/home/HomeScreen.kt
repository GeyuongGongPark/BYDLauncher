package com.bydlauncher.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(
            currentTab = currentTab,
            onOpenAppDrawer = viewModel::showAppDrawer,
            onCloseAppDrawer = viewModel::showHome,
        )
    } else {
        PortraitLayout(
            currentTab = currentTab,
            onOpenAppDrawer = viewModel::showAppDrawer,
            onCloseAppDrawer = viewModel::showHome,
        )
    }
}

@Composable
private fun LandscapeLayout(
    currentTab: HomeTab,
    onOpenAppDrawer: () -> Unit,
    onCloseAppDrawer: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        // Left side panel (clock, placeholders)
        SidePanel()

        // Main content area
        Column(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = currentTab,
                modifier = Modifier.weight(1f),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition",
            ) { tab ->
                when (tab) {
                    HomeTab.HOME -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BackgroundDeep),
                    )
                    HomeTab.APP_DRAWER -> AppDrawer()
                }
            }

            AppDock(
                onOpenAppDrawer = {
                    if (currentTab == HomeTab.APP_DRAWER) onCloseAppDrawer() else onOpenAppDrawer()
                },
            )
        }
    }
}

@Composable
private fun PortraitLayout(
    currentTab: HomeTab,
    onOpenAppDrawer: () -> Unit,
    onCloseAppDrawer: () -> Unit,
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
            label = "tab_transition",
        ) { tab ->
            when (tab) {
                HomeTab.HOME -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDeep),
                )
                HomeTab.APP_DRAWER -> AppDrawer()
            }
        }

        AppDock(
            onOpenAppDrawer = {
                if (currentTab == HomeTab.APP_DRAWER) onCloseAppDrawer() else onOpenAppDrawer()
            },
        )
    }
}

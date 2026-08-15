package com.bydlauncher.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.ui.components.CalendarCard
import com.bydlauncher.ui.components.ClockWidget
import com.bydlauncher.ui.components.WeatherCard
import com.bydlauncher.ui.navi.NaviSection
import com.bydlauncher.ui.sidepanel.SidePanelViewModel

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
fun LandscapeHomeContent(modifier: Modifier = Modifier) {
    NaviSection(modifier = modifier)
}

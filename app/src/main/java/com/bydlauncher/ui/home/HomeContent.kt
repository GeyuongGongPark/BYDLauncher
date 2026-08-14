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
import com.bydlauncher.ui.sidepanel.SidePanelViewModel

/**
 * Portrait 홈 탭: 시계 + 날씨 + 캘린더 수직 레이아웃.
 * Landscape에서는 SidePanel이 이 역할을 하므로, 메인 영역은 단순 위젯만 표시.
 */
@Composable
fun PortraitHomeContent(
    modifier: Modifier = Modifier,
    viewModel: SidePanelViewModel = hiltViewModel(),
) {
    val weatherState by viewModel.weatherState.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        ClockWidget(showDate = true)

        Spacer(Modifier.height(24.dp))

        WeatherCard(
            state = weatherState,
            onRetry = viewModel::refreshWeather,
        )

        Spacer(Modifier.height(12.dp))

        CalendarCard(events = calendarEvents)
    }
}

/**
 * Landscape 홈 탭 메인 영역: SidePanel이 이미 정보를 표시하므로
 * 추가 날씨/시계 없이 빠른 앱 접근용 빈 공간으로 유지.
 * 추후 즐겨찾기 앱 위젯으로 확장 예정.
 */
@Composable
fun LandscapeHomeContent(modifier: Modifier = Modifier) {
    // 현재는 순수 빈 공간 — 드래그 앤 드롭 즐겨찾기로 Phase 3에서 확장
}

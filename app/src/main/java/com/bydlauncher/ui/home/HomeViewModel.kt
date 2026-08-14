package com.bydlauncher.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class HomeTab { HOME, APP_DRAWER }

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _currentTab = MutableStateFlow(HomeTab.HOME)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    fun showHome() {
        _currentTab.value = HomeTab.HOME
    }

    fun showAppDrawer() {
        _currentTab.value = HomeTab.APP_DRAWER
    }

    fun toggleAppDrawer() {
        _currentTab.value = if (_currentTab.value == HomeTab.APP_DRAWER) {
            HomeTab.HOME
        } else {
            HomeTab.APP_DRAWER
        }
    }
}

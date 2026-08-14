package com.bydlauncher.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.domain.apps.AppInfo
import com.bydlauncher.domain.apps.AppRepository
import com.bydlauncher.domain.favorites.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeTab { HOME, APP_DRAWER }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    appRepository: AppRepository,
) : ViewModel() {

    private val _currentTab = MutableStateFlow(HomeTab.HOME)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    private val _isEditingFavorites = MutableStateFlow(false)
    val isEditingFavorites: StateFlow<Boolean> = _isEditingFavorites.asStateFlow()

    // 즐겨찾기 패키지명 순서 유지 + 실제 AppInfo 매핑
    val favoriteApps: StateFlow<List<AppInfo>> = combine(
        favoritesRepository.getFavoritePackages(),
        appRepository.getInstalledApps(),
    ) { pkgs, allApps ->
        val appMap = allApps.associateBy { it.packageName }
        pkgs.mapNotNull { appMap[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // 앱 선택 다이얼로그용 — 즐겨찾기 미포함 앱 목록
    val nonFavoriteApps: StateFlow<List<AppInfo>> = combine(
        favoritesRepository.getFavoritePackages(),
        appRepository.getInstalledApps(),
    ) { pkgs, allApps ->
        allApps.filter { it.packageName !in pkgs }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleAppDrawer() {
        _currentTab.value = if (_currentTab.value == HomeTab.APP_DRAWER) HomeTab.HOME else HomeTab.APP_DRAWER
    }

    fun toggleEditFavorites() {
        _isEditingFavorites.value = !_isEditingFavorites.value
    }

    fun exitEditFavorites() {
        _isEditingFavorites.value = false
    }

    fun addFavorite(packageName: String) = viewModelScope.launch {
        favoritesRepository.add(packageName)
    }

    fun removeFavorite(packageName: String) = viewModelScope.launch {
        favoritesRepository.remove(packageName)
    }
}

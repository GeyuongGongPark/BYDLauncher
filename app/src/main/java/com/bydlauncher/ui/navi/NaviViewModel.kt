package com.bydlauncher.ui.navi

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.domain.navi.NaviApp
import com.bydlauncher.domain.navi.NaviRepository
import com.bydlauncher.domain.navi.SUPPORTED_NAVI_APPS
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NaviViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val naviRepository: NaviRepository,
) : ViewModel() {

    /** 기기에 설치된 지원 네비 앱만 필터링 */
    val installedNaviApps: List<NaviApp> = SUPPORTED_NAVI_APPS.filter { app ->
        isInstalled(app.packageName)
    }

    /** 선택된 네비 앱 (설치 여부 재검증 포함) */
    val selectedNaviApp: StateFlow<NaviApp?> = naviRepository.getSelectedPackage()
        .map { pkg ->
            pkg?.let { p ->
                SUPPORTED_NAVI_APPS.find { it.packageName == p }
                    ?.takeIf { isInstalled(p) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectNaviApp(packageName: String) = viewModelScope.launch {
        naviRepository.setSelectedPackage(packageName)
    }

    fun clearNaviApp() = viewModelScope.launch {
        naviRepository.setSelectedPackage(null)
    }

    private fun isInstalled(packageName: String): Boolean =
        try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
}

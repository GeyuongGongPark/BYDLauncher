package com.bydlauncher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bydlauncher.data.settings.AppSettingsRepositoryImpl
import com.bydlauncher.overlay.OverlayService
import com.bydlauncher.ui.home.HomeScreen
import com.bydlauncher.ui.theme.BYDLauncherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: AppSettingsRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kinex와 동일: Manifest는 enabled=false로 설치 차단 우회,
        // 실행 후 즉시 enabled=true로 전환하여 정상 홈 앱으로 동작
        enableSelf()

        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState(initial = com.bydlauncher.ui.theme.ThemeMode.DARK)
            BYDLauncherTheme(themeMode = themeMode) {
                HomeScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        // 네비에서 홈으로 돌아오면 오버레이 중지
        startService(Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_HIDE
        })
    }

    private fun enableSelf() {
        try {
            val pm = packageManager
            val cn = ComponentName(this, MainActivity::class.java)
            if (pm.getComponentEnabledSetting(cn) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(
                    cn,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        } catch (_: Exception) {}
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    companion object {
        /** 현재 기본 홈 앱이 BYDLauncher인지 확인 */
        fun isDefaultHome(activity: ComponentActivity): Boolean = runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            val resolved = activity.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            resolved?.activityInfo?.packageName == activity.packageName
        }.getOrDefault(false)

        /** 기본 홈 앱 설정 화면 열기 */
        fun openDefaultHomeSettings(activity: ComponentActivity) {
            // 1순위: HOME_SETTINGS (표준)
            runCatching {
                activity.startActivity(Intent("android.settings.HOME_SETTINGS"))
                return
            }
            // 2순위: MANAGE_DEFAULT_APPS_SETTINGS
            runCatching {
                activity.startActivity(Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"))
                return
            }
            // 3순위: ResolverActivity 직접 (DiLink fallback)
            runCatching {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                    setClassName("android", "com.android.internal.app.ResolverActivity")
                }
                activity.startActivity(intent)
            }
        }
    }
}

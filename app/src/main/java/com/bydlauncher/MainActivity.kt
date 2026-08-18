package com.bydlauncher

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bydlauncher.ui.home.HomeScreen
import com.bydlauncher.ui.theme.BYDLauncherTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 설치 후 첫 실행 시 HomeLauncher alias 활성화
        enableHomeLauncher()

        // 기본 홈 앱이 아니면 설정 화면으로 유도
        if (!isDefaultLauncher()) {
            requestSetAsDefaultHome()
        }

        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            BYDLauncherTheme {
                HomeScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * HomeLauncher activity-alias 활성화.
     * 설치 시 disabled 상태로 배포되어 DiLink 설치 차단을 우회.
     * 앱 첫 실행 시 enabled로 전환하여 기본 홈 앱 목록에 노출.
     */
    private fun enableHomeLauncher() {
        val alias = ComponentName(this, "com.bydlauncher.HomeLauncher")
        val state = packageManager.getComponentEnabledSetting(alias)
        if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            packageManager.setComponentEnabledSetting(
                alias,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
        }
    }

    /** 현재 앱이 기본 홈 앱인지 확인 */
    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    /** 기본 홈 설정 화면으로 이동 */
    private fun requestSetAsDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                startActivityForResult(
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME),
                    REQUEST_SET_HOME,
                )
                return
            }
        }
        // Android 9 (API 28) fallback
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
    }

    companion object {
        private const val REQUEST_SET_HOME = 1001
    }
}

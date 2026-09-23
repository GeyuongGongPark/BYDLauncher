package com.bydlauncher.ui.navi

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * dadb 라이브러리로 127.0.0.1:5555(ADB loopback) 연결 후
 * am start --display <displayId>로 네비 앱을 VirtualDisplay에서 실행.
 * Wi-Fi ADB가 켜진 상태에서 차량 내부에서 loopback 접근 가능.
 */
object AdbNaviLauncher {

    private const val TAG = "AdbNaviLauncher"
    private const val ADB_HOST = "127.0.0.1"
    private const val ADB_PORT = 5555

    /**
     * 네비 앱을 VirtualDisplay에서 실행하고, 2초 후 런처를 포그라운드로 복귀.
     */
    suspend fun launch(
        context: Context,
        packageName: String,
        displayId: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val keyPair = getOrCreateKeyPair(context)
            val activity = getLaunchActivity(context, packageName)
                ?: error("$packageName 실행 가능한 Activity 없음")

            Dadb.create(ADB_HOST, ADB_PORT, keyPair).use { adb ->
                val startCmd = "am start --display $displayId -n $packageName/$activity"
                val startResult = adb.shell(startCmd)
                Log.i(TAG, "am start 결과: ${startResult.output.trim()}")
                check(startResult.exitCode == 0) { "am start 실패: ${startResult.output}" }

                // 2초 후 런처 포그라운드 복귀
                adb.shell("sleep 2 ; am start -n ${context.packageName}/.MainActivityLauncher")
                Unit
            }
        }.onFailure {
            Log.w(TAG, "네비 실행 실패: ${it.message}")
        }
    }

    /**
     * VirtualDisplay에 터치 이벤트 주입.
     */
    suspend fun injectTouch(
        context: Context,
        displayId: Int,
        action: Int,
        x: Int,
        y: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_MOVE) {
                return@runCatching
            }
            val keyPair = getOrCreateKeyPair(context)
            Dadb.create(ADB_HOST, ADB_PORT, keyPair).use { adb ->
                adb.shell("input -d $displayId tap $x $y")
                Unit
            }
        }.onFailure {
            Log.w(TAG, "터치 주입 실패: ${it.message}")
        }
    }

    private fun getOrCreateKeyPair(context: Context): AdbKeyPair {
        val keyDir = File(context.filesDir, "adb_keys").also { it.mkdirs() }
        val privateKey = File(keyDir, "adb.key")
        val publicKey = File(keyDir, "adb.pub")
        if (!privateKey.exists() || !publicKey.exists()) {
            AdbKeyPair.generate(privateKey, publicKey)
        }
        return AdbKeyPair.read(privateKey, publicKey)
    }

    private fun getLaunchActivity(context: Context, packageName: String): String? {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return null
        return intent.component?.className
            ?: context.packageManager
                .getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                .activities?.firstOrNull()?.name
    }
}

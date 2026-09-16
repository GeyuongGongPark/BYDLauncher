package com.bydlauncher.ui.navi

import android.content.Context
import android.content.pm.PackageManager
import android.view.MotionEvent
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ADB loopback(127.0.0.1:5555)으로 am start --display <displayId> 실행.
 * T맵 실행 후 같은 ADB 세션에서 즉시 런처 포그라운드 복귀.
 */
object AdbNaviLauncher {

    private const val ADB_HOST = "127.0.0.1"
    private const val ADB_PORT = 5555

    /**
     * T맵을 VirtualDisplay에서 실행하고, 런처를 포그라운드로 복귀.
     * 하나의 ADB 세션에서 두 명령을 연속 실행해 타이밍 문제 최소화.
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
                // T맵을 VirtualDisplay에서 실행
                val startCmd = "am start --display $displayId -n $packageName/$activity"
                val startResult = adb.shell(startCmd)
                check(startResult.exitCode == 0) { "am start 실패: ${startResult.output}" }

                // 2초 대기 후 런처를 포그라운드로 복귀
                // MainActivityLauncher alias 사용 (Kinex와 동일한 패턴)
                adb.shell("sleep 2 ; am start -n ${context.packageName}/.MainActivityLauncher")
                Unit
            }
        }
    }

    /**
     * VirtualDisplay에 터치 이벤트 주입.
     * 좌표는 호출부에서 VirtualDisplay 크기로 이미 변환된 값.
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

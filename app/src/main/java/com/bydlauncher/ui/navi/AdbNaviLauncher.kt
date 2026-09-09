package com.bydlauncher.ui.navi

import android.content.Context
import android.content.pm.PackageManager
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ADB loopback(127.0.0.1:5555)으로 am start --display <displayId> 실행.
 * Kinex와 동일한 방식으로 VirtualDisplay에 네비 앱 임베딩.
 */
object AdbNaviLauncher {

    private const val ADB_HOST = "127.0.0.1"
    private const val ADB_PORT = 5555

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
                val cmd = "am start --display $displayId -n $packageName/$activity"
                val result = adb.shell(cmd)
                check(result.exitCode == 0) { "am start 실패: ${result.output}" }
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

package com.bydlauncher.data.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.bydlauncher.domain.apps.AppInfo
import com.bydlauncher.domain.apps.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppRepository {

    override fun getInstalledApps(): Flow<List<AppInfo>> = callbackFlow {
        // 초기 목록
        launch { send(queryApps()) }

        // 앱 설치/제거/업데이트 감지
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                launch { send(queryApps()) }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter)
        awaitClose { context.unregisterReceiver(receiver) }
    }.flowOn(Dispatchers.IO)

    private fun queryApps(): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                AppInfo(
                    packageName = info.activityInfo.packageName,
                    label = info.loadLabel(pm).toString(),
                    icon = info.loadIcon(pm),
                )
            }
            .sortedBy { it.label.lowercase() }
    }
}

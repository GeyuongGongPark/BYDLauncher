package com.bydlauncher.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.bydlauncher.domain.apps.AppInfo
import com.bydlauncher.domain.apps.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppRepository {

    override fun getInstalledApps(): Flow<List<AppInfo>> = flow {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        val apps = resolveInfos
            .filter { it.activityInfo.packageName != context.packageName }
            .map { info ->
                AppInfo(
                    packageName = info.activityInfo.packageName,
                    label = info.loadLabel(pm).toString(),
                    icon = info.loadIcon(pm),
                )
            }
            .sortedBy { it.label.lowercase() }
        emit(apps)
    }.flowOn(Dispatchers.IO)
}

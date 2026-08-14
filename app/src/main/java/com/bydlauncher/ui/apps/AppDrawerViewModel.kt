package com.bydlauncher.ui.apps

import androidx.lifecycle.ViewModel
import com.bydlauncher.domain.apps.AppInfo
import com.bydlauncher.domain.apps.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class AppDrawerViewModel @Inject constructor(
    appRepository: AppRepository,
) : ViewModel() {

    val apps: Flow<List<AppInfo>> = appRepository.getInstalledApps()
}

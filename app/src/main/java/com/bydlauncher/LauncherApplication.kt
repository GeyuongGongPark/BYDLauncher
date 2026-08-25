package com.bydlauncher

import android.app.Application
import com.bydlauncher.camping.CampingState
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.MutableStateFlow

@HiltAndroidApp
class LauncherApplication : Application() {
    /** CampingService → CampingViewModel 상태 공유 채널 */
    val campingState = MutableStateFlow<CampingState>(CampingState.Idle)
}

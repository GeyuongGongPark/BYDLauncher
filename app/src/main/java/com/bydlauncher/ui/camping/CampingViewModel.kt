package com.bydlauncher.ui.camping

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.LauncherApplication
import com.bydlauncher.camping.CampingState
import com.bydlauncher.camping.service.CampingService
import com.bydlauncher.camping.storage.CampingPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CampingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val prefs: CampingPrefs,
) : ViewModel() {

    val state: StateFlow<CampingState> =
        (context.applicationContext as LauncherApplication).campingState
            .stateIn(viewModelScope, SharingStarted.Eagerly, CampingState.Idle)

    fun startCamping() {
        val intent = Intent(context, CampingService::class.java)
        context.startForegroundService(intent)
    }

    fun stopCamping() {
        val intent = Intent(context, CampingService::class.java).apply {
            action = CampingService.ACTION_STOP
        }
        context.startService(intent)
    }
}

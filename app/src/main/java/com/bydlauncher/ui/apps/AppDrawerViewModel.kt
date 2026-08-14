package com.bydlauncher.ui.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.domain.apps.AppInfo
import com.bydlauncher.domain.apps.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppDrawerViewModel @Inject constructor(
    appRepository: AppRepository,
) : ViewModel() {

    val query = MutableStateFlow("")

    val apps: StateFlow<List<AppInfo>> = combine(
        appRepository.getInstalledApps(),
        query,
    ) { list, q ->
        if (q.isBlank()) list
        else list.filter { it.label.contains(q.trim(), ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

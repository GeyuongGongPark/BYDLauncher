package com.bydlauncher.ui.kbo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bydlauncher.domain.kbo.KboGameState
import com.bydlauncher.domain.kbo.KboRepository
import com.bydlauncher.domain.kbo.KboTeam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KboViewModel @Inject constructor(
    private val repository: KboRepository,
) : ViewModel() {

    private val _gameState = MutableStateFlow<KboGameState>(KboGameState.NoTeam)
    val gameState: StateFlow<KboGameState> = _gameState.asStateFlow()

    private val _favoriteTeam = MutableStateFlow<KboTeam?>(null)
    val favoriteTeam: StateFlow<KboTeam?> = _favoriteTeam.asStateFlow()

    private var pollingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getFavoriteTeam().collect { team ->
                _favoriteTeam.value = team
                if (team == null) {
                    _gameState.value = KboGameState.NoTeam
                    pollingJob?.cancel()
                } else {
                    startPolling(team)
                }
            }
        }
    }

    fun selectTeam(team: KboTeam) {
        viewModelScope.launch {
            repository.setFavoriteTeam(team)
        }
    }

    fun clearTeam() {
        viewModelScope.launch {
            repository.clearFavoriteTeam()
        }
    }

    private fun startPolling(team: KboTeam) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                val state = repository.getGameState(team)
                _gameState.value = state
                val interval = when (state) {
                    is KboGameState.Live -> 30_000L
                    is KboGameState.Scheduled -> 60_000L
                    else -> 5 * 60_000L
                }
                delay(interval)
            }
        }
    }

    fun refresh() {
        val team = _favoriteTeam.value ?: return
        viewModelScope.launch {
            _gameState.value = repository.getGameState(team)
        }
    }
}

package com.bydlauncher.domain.kbo

import kotlinx.coroutines.flow.Flow

interface KboRepository {
    fun getFavoriteTeam(): Flow<KboTeam?>
    suspend fun setFavoriteTeam(team: KboTeam)
    suspend fun clearFavoriteTeam()
    suspend fun getGameState(team: KboTeam): KboGameState
    suspend fun getRecentResults(team: KboTeam): List<GameResult>
}

package com.bydlauncher.data.kbo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bydlauncher.domain.kbo.GameResult
import com.bydlauncher.domain.kbo.KboGameState
import com.bydlauncher.domain.kbo.KboRepository
import com.bydlauncher.domain.kbo.KboTeam
import com.bydlauncher.domain.kbo.SeriesRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.kboDataStore by preferencesDataStore(name = "kbo_settings")

// Mock 시나리오 전환용 — API 연동 전 UI 검증용
enum class KboMockScenario {
    NO_GAME,
    SCHEDULED,
    LIVE,
    FINAL_WIN,
    POSTSEASON_LIVE,
    OFF_SEASON,
}

// 개발 중 시나리오 변경: KboRepositoryMock.activeScenario = KboMockScenario.LIVE
var activeMockScenario: KboMockScenario = KboMockScenario.POSTSEASON_LIVE

@Singleton
class KboRepositoryMock @Inject constructor(
    @ApplicationContext private val context: Context,
) : KboRepository {

    private val favoriteTeamKey = stringPreferencesKey("kbo_favorite_team")

    private val recentResults = listOf(
        GameResult.WIN,
        GameResult.WIN,
        GameResult.LOSE,
        GameResult.WIN,
        GameResult.LOSE,
    )

    override fun getFavoriteTeam(): Flow<KboTeam?> =
        context.kboDataStore.data.map { prefs ->
            prefs[favoriteTeamKey]?.let { name ->
                runCatching { KboTeam.valueOf(name) }.getOrNull()
            }
        }

    override suspend fun setFavoriteTeam(team: KboTeam) {
        context.kboDataStore.edit { prefs ->
            prefs[favoriteTeamKey] = team.name
        }
    }

    override suspend fun clearFavoriteTeam() {
        context.kboDataStore.edit { prefs ->
            prefs.remove(favoriteTeamKey)
        }
    }

    override suspend fun getGameState(team: KboTeam): KboGameState =
        when (activeMockScenario) {
            KboMockScenario.NO_GAME -> KboGameState.NoGame(
                rank = 3,
                recentResults = recentResults,
            )
            KboMockScenario.SCHEDULED -> KboGameState.Scheduled(
                opponent = KboTeam.DOOSAN,
                venue = team.homeVenue,
                startTime = "19:00",
                rank = 3,
                recentResults = recentResults,
            )
            KboMockScenario.LIVE -> KboGameState.Live(
                opponent = KboTeam.DOOSAN,
                myScore = 5,
                oppScore = 3,
                inning = 7,
                isTopInning = false,
                outs = 1,
                rank = 3,
                recentResults = recentResults,
            )
            KboMockScenario.FINAL_WIN -> KboGameState.Final(
                opponent = KboTeam.DOOSAN,
                myScore = 8,
                oppScore = 4,
                result = GameResult.WIN,
                rank = 3,
                recentResults = recentResults,
            )
            KboMockScenario.POSTSEASON_LIVE -> KboGameState.Live(
                opponent = KboTeam.SAMSUNG,
                myScore = 2,
                oppScore = 1,
                inning = 5,
                isTopInning = true,
                outs = 2,
                rank = 2,
                recentResults = recentResults,
                isPostseason = true,
                roundName = "준플레이오프",
                seriesRecord = SeriesRecord(myWins = 1, oppWins = 0),
            )
            KboMockScenario.OFF_SEASON -> KboGameState.OffSeason
        }

    override suspend fun getRecentResults(team: KboTeam): List<GameResult> = recentResults
}

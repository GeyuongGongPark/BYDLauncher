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
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val Context.kboDataStore by preferencesDataStore(name = "kbo_settings")

@Singleton
class KboRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: KboApiService,
) : KboRepository {

    private val favoriteTeamKey = stringPreferencesKey("kbo_favorite_team")

    override fun getFavoriteTeam(): Flow<KboTeam?> =
        context.kboDataStore.data.map { prefs ->
            prefs[favoriteTeamKey]?.let { name ->
                runCatching { KboTeam.valueOf(name) }.getOrNull()
            }
        }

    override suspend fun setFavoriteTeam(team: KboTeam) {
        context.kboDataStore.edit { it[favoriteTeamKey] = team.name }
    }

    override suspend fun clearFavoriteTeam() {
        context.kboDataStore.edit { it.remove(favoriteTeamKey) }
    }

    override suspend fun getGameState(team: KboTeam): KboGameState {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        val games = api.fetchGames(today)
        val standings = api.fetchStandings()
        val rank = standings.indexOfFirst { it.teamName.matchesTeam(team) }
            .takeIf { it >= 0 }?.let { it + 1 } ?: 0

        // 오늘 내 팀 경기 찾기
        val game = games.firstOrNull { g ->
            g.homeTeamName.matchesTeam(team) || g.awayTeamName.matchesTeam(team)
        } ?: return if (isOffSeason(today)) KboGameState.OffSeason
                    else KboGameState.NoGame(rank = rank, recentResults = getRecentResults(team))

        val isHome = game.homeTeamName.matchesTeam(team)
        val opponentName = if (isHome) game.awayTeamName else game.homeTeamName
        val opponent = KboTeam.entries.firstOrNull { opponentName.matchesTeam(it) }
            ?: return KboGameState.NoGame(rank = rank, recentResults = getRecentResults(team))

        val myScore = if (isHome) game.homeScore else game.awayScore
        val oppScore = if (isHome) game.awayScore else game.homeScore

        val isPostseason = game.round != null && !game.round.contains("Regular", ignoreCase = true)
        val roundName = if (isPostseason) game.round?.toKoreanRound() else null
        val seriesRecord = if (isPostseason) fetchSeriesRecord(team, opponent) else null

        val recentResults = getRecentResults(team)

        return when (game.statusShort) {
            "NS" -> {
                val time = runCatching {
                    ZonedDateTime.parse(game.date)
                        .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                }.getOrDefault("--:--")
                KboGameState.Scheduled(
                    opponent = opponent,
                    venue = if (isHome) team.homeVenue else opponent.homeVenue,
                    startTime = time,
                    rank = rank,
                    recentResults = recentResults,
                    isPostseason = isPostseason,
                    roundName = roundName,
                    seriesRecord = seriesRecord,
                )
            }
            "IP" -> KboGameState.Live(
                opponent = opponent,
                myScore = myScore ?: 0,
                oppScore = oppScore ?: 0,
                inning = game.inning ?: 1,
                isTopInning = !isHome, // free 플랜에서 상/하 미제공 시 홈팀=말 기본값
                outs = 0,              // free 플랜에서 아웃카운트 미제공
                rank = rank,
                recentResults = recentResults,
                isPostseason = isPostseason,
                roundName = roundName,
                seriesRecord = seriesRecord,
            )
            "FT" -> {
                val result = when {
                    myScore == null || oppScore == null -> GameResult.DRAW
                    myScore > oppScore -> GameResult.WIN
                    myScore < oppScore -> GameResult.LOSE
                    else -> GameResult.DRAW
                }
                KboGameState.Final(
                    opponent = opponent,
                    myScore = myScore ?: 0,
                    oppScore = oppScore ?: 0,
                    result = result,
                    rank = rank,
                    recentResults = recentResults,
                    isPostseason = isPostseason,
                    roundName = roundName,
                    seriesRecord = seriesRecord,
                )
            }
            else -> KboGameState.NoGame(rank = rank, recentResults = recentResults)
        }
    }

    override suspend fun getRecentResults(team: KboTeam): List<GameResult> {
        // 최근 5일 경기 결과 조회
        val results = mutableListOf<GameResult>()
        val zone = ZoneId.of("Asia/Seoul")
        var daysBack = 1
        while (results.size < 5 && daysBack <= 14) {
            val date = LocalDate.now(zone).minusDays(daysBack.toLong())
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
            val games = api.fetchGames(date)
            val game = games.firstOrNull { g ->
                (g.homeTeamName.matchesTeam(team) || g.awayTeamName.matchesTeam(team))
                    && g.statusShort == "FT"
            }
            if (game != null) {
                val isHome = game.homeTeamName.matchesTeam(team)
                val myScore = if (isHome) game.homeScore else game.awayScore
                val oppScore = if (isHome) game.awayScore else game.homeScore
                results += when {
                    myScore == null || oppScore == null -> GameResult.DRAW
                    myScore > oppScore -> GameResult.WIN
                    myScore < oppScore -> GameResult.LOSE
                    else -> GameResult.DRAW
                }
            }
            daysBack++
        }
        return results
    }

    // 포스트시즌 시리즈 현황: 같은 round에서 두 팀 간 전적 계산
    private suspend fun fetchSeriesRecord(team: KboTeam, opponent: KboTeam): SeriesRecord? {
        return runCatching {
            var myWins = 0
            var oppWins = 0
            var daysBack = 0
            while (daysBack <= 21) {
                val date = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(daysBack.toLong())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)
                val games = api.fetchGames(date)
                for (g in games) {
                    val isMyTeam = g.homeTeamName.matchesTeam(team) || g.awayTeamName.matchesTeam(team)
                    val isOpp = g.homeTeamName.matchesTeam(opponent) || g.awayTeamName.matchesTeam(opponent)
                    if (!isMyTeam || !isOpp || g.statusShort != "FT") continue
                    val isHome = g.homeTeamName.matchesTeam(team)
                    val myScore = if (isHome) g.homeScore else g.awayScore
                    val oppScore = if (isHome) g.awayScore else g.homeScore
                    when {
                        myScore != null && oppScore != null && myScore > oppScore -> myWins++
                        myScore != null && oppScore != null && myScore < oppScore -> oppWins++
                    }
                }
                daysBack++
            }
            SeriesRecord(myWins, oppWins)
        }.getOrNull()
    }

    private fun isOffSeason(dateStr: String): Boolean {
        val month = dateStr.substring(5, 7).toIntOrNull() ?: return false
        return month in 11..12 || month in 1..2
    }
}

// API 응답 팀명(영문) → KboTeam 매핑
private fun String.matchesTeam(team: KboTeam): Boolean {
    val name = this.lowercase()
    return when (team) {
        KboTeam.KIA -> name.contains("kia") || name.contains("tigers")
        KboTeam.SAMSUNG -> name.contains("samsung") || name.contains("lions")
        KboTeam.LG -> name.contains("lg") || name.contains("twins")
        KboTeam.DOOSAN -> name.contains("doosan") || name.contains("bears")
        KboTeam.KT -> name.contains("kt") || name.contains("wiz")
        KboTeam.SSG -> name.contains("ssg") || name.contains("landers")
        KboTeam.LOTTE -> name.contains("lotte") || name.contains("giants")
        KboTeam.HANWHA -> name.contains("hanwha") || name.contains("eagles")
        KboTeam.NC -> name.contains("nc") || name.contains("dinos")
        KboTeam.KIWOOM -> name.contains("kiwoom") || name.contains("heroes")
    }
}

private fun String.toKoreanRound(): String = when {
    contains("Korean Series", ignoreCase = true) -> "한국시리즈"
    contains("Playoff", ignoreCase = true) && !contains("Semi", ignoreCase = true) -> "플레이오프"
    contains("Semi", ignoreCase = true) -> "준플레이오프"
    contains("Wild", ignoreCase = true) -> "와일드카드"
    else -> this
}

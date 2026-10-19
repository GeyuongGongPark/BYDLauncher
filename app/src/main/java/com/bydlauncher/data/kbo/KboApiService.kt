package com.bydlauncher.data.kbo

import com.bydlauncher.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

private const val BASE_URL = "https://v1.baseball.api-sports.io"

// api-sports.io Baseball API 응답 구조
data class ApiGame(
    val id: Int,
    val date: String,           // ISO8601, e.g. "2026-10-19T10:00:00+09:00"
    val statusShort: String,    // "NS"=예정, "IP"=진행중, "FT"=종료
    val round: String?,         // "Regular Season", "Semi-Playoffs", "Playoffs", "Korean Series"
    val homeTeamName: String,
    val awayTeamName: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val inning: Int?,           // periods.current (free 플랜 제공 여부 미확정)
)

data class ApiStanding(
    val teamName: String,
    val position: Int,
)

@Singleton
class KboApiService @Inject constructor(
    private val client: OkHttpClient,
) {
    private val apiKey = BuildConfig.KBO_API_KEY

    // KBO league ID — /leagues?name=KBO 로 조회해서 확인 필요
    // api-sports.io 기준 KBO = 26 (초기값, 실제 응답으로 덮어씀)
    private var kboLeagueId: Int = 26

    suspend fun fetchLeagueId(): Int = withContext(Dispatchers.IO) {
        runCatching {
            val body = get("/leagues?country=South+Korea&type=League")
            val arr = body.getJSONArray("response")
            for (i in 0 until arr.length()) {
                val league = arr.getJSONObject(i).getJSONObject("league")
                val name = league.getString("name")
                if (name.contains("KBO", ignoreCase = true)) {
                    kboLeagueId = league.getInt("id")
                    return@runCatching kboLeagueId
                }
            }
            kboLeagueId
        }.getOrDefault(kboLeagueId)
    }

    suspend fun fetchGames(date: String): List<ApiGame> = withContext(Dispatchers.IO) {
        runCatching {
            val body = get("/games?league=$kboLeagueId&season=2026&date=$date")
            val arr = body.getJSONArray("response")
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                val status = obj.getJSONObject("status")
                val teams = obj.getJSONObject("teams")
                val scores = obj.getJSONObject("scores")
                val league = obj.getJSONObject("league")

                ApiGame(
                    id = obj.getInt("id"),
                    date = obj.getString("date"),
                    statusShort = status.getString("short"),
                    round = league.optString("round").takeIf { it.isNotBlank() },
                    homeTeamName = teams.getJSONObject("home").getString("name"),
                    awayTeamName = teams.getJSONObject("away").getString("name"),
                    homeScore = scores.getJSONObject("home").optInt("total", -1).takeIf { it >= 0 },
                    awayScore = scores.getJSONObject("away").optInt("total", -1).takeIf { it >= 0 },
                    inning = obj.optJSONObject("periods")?.optInt("current")?.takeIf { it > 0 },
                )
            }
        }.getOrDefault(emptyList())
    }

    suspend fun fetchStandings(): List<ApiStanding> = withContext(Dispatchers.IO) {
        runCatching {
            val body = get("/standings?league=$kboLeagueId&season=2026")
            val arr = body.getJSONArray("response")
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                ApiStanding(
                    teamName = obj.getJSONObject("team").getString("name"),
                    position = obj.getInt("position"),
                )
            }
        }.getOrDefault(emptyList())
    }

    private fun get(path: String): JSONObject {
        val request = Request.Builder()
            .url("$BASE_URL$path")
            .addHeader("x-apisports-key", apiKey)
            .build()
        val body = client.newCall(request).execute().use { resp ->
            check(resp.isSuccessful) { "HTTP ${resp.code}" }
            resp.body?.string() ?: error("빈 응답")
        }
        return JSONObject(body)
    }
}

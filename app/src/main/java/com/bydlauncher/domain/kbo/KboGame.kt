package com.bydlauncher.domain.kbo

enum class GameResult { WIN, LOSE, DRAW }

data class SeriesRecord(val myWins: Int, val oppWins: Int)

sealed class KboGameState {

    data object NoTeam : KboGameState()

    data class NoGame(
        val rank: Int,
        val recentResults: List<GameResult>,
    ) : KboGameState()

    data class Scheduled(
        val opponent: KboTeam,
        val venue: String,
        val startTime: String,
        val rank: Int,
        val recentResults: List<GameResult>,
        val isPostseason: Boolean = false,
        val roundName: String? = null,
        val seriesRecord: SeriesRecord? = null,
    ) : KboGameState()

    data class Live(
        val opponent: KboTeam,
        val myScore: Int,
        val oppScore: Int,
        val inning: Int,
        val isTopInning: Boolean,
        val outs: Int,
        val rank: Int,
        val recentResults: List<GameResult>,
        val isPostseason: Boolean = false,
        val roundName: String? = null,
        val seriesRecord: SeriesRecord? = null,
    ) : KboGameState()

    data class Final(
        val opponent: KboTeam,
        val myScore: Int,
        val oppScore: Int,
        val result: GameResult,
        val rank: Int,
        val recentResults: List<GameResult>,
        val isPostseason: Boolean = false,
        val roundName: String? = null,
        val seriesRecord: SeriesRecord? = null,
    ) : KboGameState()

    data object OffSeason : KboGameState()
}

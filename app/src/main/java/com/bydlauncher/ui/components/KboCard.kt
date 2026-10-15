package com.bydlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bydlauncher.domain.kbo.GameResult
import com.bydlauncher.domain.kbo.KboGameState
import com.bydlauncher.domain.kbo.KboTeam
import com.bydlauncher.domain.kbo.SeriesRecord
import com.bydlauncher.ui.kbo.KboViewModel
import com.bydlauncher.ui.theme.BackgroundCard
import com.bydlauncher.ui.theme.DividerColor
import com.bydlauncher.ui.theme.TextDisabled
import com.bydlauncher.ui.theme.TextPrimary
import com.bydlauncher.ui.theme.TextSecondary

@Composable
fun KboCard(
    modifier: Modifier = Modifier,
    viewModel: KboViewModel = hiltViewModel(),
) {
    val gameState by viewModel.gameState.collectAsState()
    val team by viewModel.favoriteTeam.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .padding(12.dp),
    ) {
        KboCardHeader(team = team, gameState = gameState)

        if (gameState !is KboGameState.NoTeam) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            KboCardBody(gameState = gameState)
        }

        val recentResults = when (gameState) {
            is KboGameState.NoGame -> (gameState as KboGameState.NoGame).recentResults
            is KboGameState.Scheduled -> (gameState as KboGameState.Scheduled).recentResults
            is KboGameState.Live -> (gameState as KboGameState.Live).recentResults
            is KboGameState.Final -> (gameState as KboGameState.Final).recentResults
            else -> null
        }
        if (!recentResults.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Spacer(Modifier.height(8.dp))
            RecentResultsRow(results = recentResults)
        }
    }
}

@Composable
private fun KboCardHeader(team: KboTeam?, gameState: KboGameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "⚾",
            fontSize = 13.sp,
        )
        Spacer(Modifier.width(6.dp))

        if (team != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(team.color),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = team.shortName,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Text(
                text = "KBO",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(Modifier.weight(1f))

        val rank = when (gameState) {
            is KboGameState.NoGame -> gameState.rank
            is KboGameState.Scheduled -> gameState.rank
            is KboGameState.Live -> gameState.rank
            is KboGameState.Final -> gameState.rank
            else -> null
        }
        if (rank != null) {
            Text(
                text = "${rank}위",
                color = TextSecondary,
                fontSize = 11.sp,
            )
        }
    }

    // 포스트시즌 배지
    val roundName = when (gameState) {
        is KboGameState.Scheduled -> if (gameState.isPostseason) gameState.roundName else null
        is KboGameState.Live -> if (gameState.isPostseason) gameState.roundName else null
        is KboGameState.Final -> if (gameState.isPostseason) gameState.roundName else null
        else -> null
    }
    if (roundName != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = "PO  $roundName",
            color = Color(0xFFFFB830),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFFB830).copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun KboCardBody(gameState: KboGameState) {
    when (gameState) {
        is KboGameState.NoTeam -> Unit

        is KboGameState.NoGame -> {
            Text(
                text = "오늘 경기 없음",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }

        is KboGameState.OffSeason -> {
            Text(
                text = "시즌 종료",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }

        is KboGameState.Scheduled -> {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "vs ${gameState.opponent.fullName}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = gameState.venue,
                    color = TextSecondary,
                    fontSize = 11.sp,
                )
                Text(
                    text = gameState.startTime,
                    color = TextSecondary,
                    fontSize = 11.sp,
                )
                gameState.seriesRecord?.let { SeriesRecordRow(it) }
            }
        }

        is KboGameState.Live -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 이닝 + 아웃카운트
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${gameState.inning}회${if (gameState.isTopInning) "초" else "말"}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                    )
                    OutIndicator(outs = gameState.outs)
                }
                // 스코어
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "${gameState.myScore}",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = ":",
                        color = TextDisabled,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "${gameState.oppScore}",
                        color = TextSecondary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = gameState.opponent.shortName,
                        color = TextDisabled,
                        fontSize = 11.sp,
                    )
                }
                gameState.seriesRecord?.let { SeriesRecordRow(it) }
            }
        }

        is KboGameState.Final -> {
            val resultColor = when (gameState.result) {
                GameResult.WIN -> Color(0xFF4CAF50)
                GameResult.LOSE -> Color(0xFFF44336)
                GameResult.DRAW -> TextSecondary
            }
            val resultText = when (gameState.result) {
                GameResult.WIN -> "승"
                GameResult.LOSE -> "패"
                GameResult.DRAW -> "무"
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = resultText,
                        color = resultColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(resultColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    Text(
                        text = "${gameState.myScore}  :  ${gameState.oppScore}",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = gameState.opponent.shortName,
                        color = TextDisabled,
                        fontSize = 11.sp,
                    )
                }
                gameState.seriesRecord?.let { SeriesRecordRow(it) }
            }
        }
    }
}

@Composable
private fun OutIndicator(outs: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < outs) Color(0xFFFFB830) else TextDisabled
                    ),
            )
        }
        Spacer(Modifier.width(2.dp))
        Text(
            text = "${outs}아웃",
            color = TextSecondary,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun SeriesRecordRow(record: SeriesRecord) {
    Text(
        text = "시리즈  ${record.myWins}승 ${record.oppWins}패",
        color = Color(0xFFFFB830).copy(alpha = 0.8f),
        fontSize = 10.sp,
    )
}

@Composable
private fun RecentResultsRow(results: List<GameResult>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "최근",
            color = TextDisabled,
            fontSize = 10.sp,
        )
        Spacer(Modifier.width(2.dp))
        results.forEach { result ->
            val (color, label) = when (result) {
                GameResult.WIN -> Color(0xFF4CAF50) to "W"
                GameResult.LOSE -> Color(0xFFF44336) to "L"
                GameResult.DRAW -> TextSecondary to "D"
            }
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

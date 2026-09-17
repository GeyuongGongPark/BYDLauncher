package com.bydlauncher.domain.drive

/**
 * 에코 점수 계산기.
 * 기준: 100점. 급가속 -5, 급제동 -3, 회생제동 +1 (최대 +10).
 */
object EcoScoreCalculator {

    fun calculate(
        harshAccelCount: Int,
        harshBrakeCount: Int,
        regenCount: Int,
    ): Int {
        val regenBonus = minOf(regenCount, 10)
        val score = 100 - harshAccelCount * 5 - harshBrakeCount * 3 + regenBonus
        return score.coerceIn(0, 100)
    }
}

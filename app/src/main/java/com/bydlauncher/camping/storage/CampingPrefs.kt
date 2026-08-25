package com.bydlauncher.camping.storage

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CampingPrefs @Inject constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("camping_prefs", Context.MODE_PRIVATE)

    /** 목표 온도 (°C, 16~30) */
    var targetTemp: Int
        get() = prefs.getInt("targetTemp", 24)
        set(v) = prefs.edit().putInt("targetTemp", v.coerceIn(16, 30)).apply()

    /** 에어컨 종료 배터리% (10~50) */
    var stopBatteryPct: Int
        get() = prefs.getInt("stopBatteryPct", 30)
        set(v) = prefs.edit().putInt("stopBatteryPct", v.coerceIn(10, 50)).apply()

    /** 최대 지속 시간 (시간, 1~12) */
    var maxHours: Int
        get() = prefs.getInt("maxHours", 8)
        set(v) = prefs.edit().putInt("maxHours", v.coerceIn(1, 12)).apply()
}

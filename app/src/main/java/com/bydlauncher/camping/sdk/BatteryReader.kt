package com.bydlauncher.camping.sdk

import android.content.Context
import android.util.Log

/**
 * BYDAutoStatisticDevice.getESTIMATE_SOC_V1() 래퍼.
 * 지원되지 않으면 -1 반환 (시간 기반 종료만 사용).
 */
class BatteryReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "BatteryReader"
        private const val STATISTIC_CLASS = "android.hardware.bydauto.statistic.BYDAutoStatisticDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
        private const val INVALID = 65535
        private const val INVALID2 = -10011
    }

    fun connect() {
        if (device != null) return

        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "StatisticDevice 로딩 성공 (시스템)"); return }

        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir,
                    ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir,
                    ctx.classLoader,
                )
                device = getInstance(cl)
                Log.i(TAG, "StatisticDevice 로딩 성공: $pkg")
            }.onSuccess { return }
        }
        Log.w(TAG, "StatisticDevice 로딩 실패 — 배터리 감시 비활성")
    }

    /** 배터리 SOC (%). 조회 불가 시 -1 반환 */
    fun getSoc(): Int {
        val d = device ?: return -1
        // 공식 API 우선
        runCatching {
            val pct = d.javaClass.getMethod("getElecPercentageValue").invoke(d) as Double
            if (pct in 0.0..100.0) return pct.toInt()
        }
        // fallback: 비공식 API
        return runCatching {
            val raw = d.javaClass.getMethod("getESTIMATE_SOC_V1").invoke(d) as Int
            if (raw == INVALID || raw == INVALID2 || raw < 0 || raw > 100) -1 else raw
        }.getOrDefault(-1)
    }

    /** 순수 전기 주행 가능 거리 (km). 조회 불가 시 -1 반환 */
    fun getElecDrivingRangeKm(): Int =
        runCatching {
            val d = device ?: return -1
            val raw = d.javaClass.getMethod("getElecDrivingRangeValue").invoke(d) as Int
            if (raw <= 0 || raw > 9999) -1 else raw
        }.getOrDefault(-1)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(STATISTIC_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoStatisticDevice 래퍼.
 * EV 배터리%, 주행 가능 거리, 연료 잔량을 조회한다.
 */
class StatisticReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "StatisticReader"
        private const val CLASS = "android.hardware.bydauto.statistic.BYDAutoStatisticDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "StatisticDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "StatisticDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "StatisticDevice 로딩 실패")
    }

    /** EV 배터리 잔량 (0.0~100.0). 조회 불가 시 -1.0 */
    fun getBatteryPct(): Double =
        runCatching {
            val d = device ?: return -1.0
            val pct = d.javaClass.getMethod("getElecPercentageValue").invoke(d) as Double
            if (pct in 0.0..100.0) pct else -1.0
        }.getOrDefault(-1.0)

    /** EV 주행 가능 거리 (km). 조회 불가 시 -1 */
    fun getElecRangeKm(): Int =
        runCatching {
            val d = device ?: return -1
            val raw = d.javaClass.getMethod("getElecDrivingRangeValue").invoke(d) as Int
            if (raw in 1..9999) raw else -1
        }.getOrDefault(-1)

    /** 연료 잔량 (0~100). 전기차는 -1 */
    fun getFuelPct(): Int =
        runCatching {
            val d = device ?: return -1
            val raw = d.javaClass.getMethod("getFuelPercentageValue").invoke(d) as Int
            if (raw in 0..100) raw else -1
        }.getOrDefault(-1)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

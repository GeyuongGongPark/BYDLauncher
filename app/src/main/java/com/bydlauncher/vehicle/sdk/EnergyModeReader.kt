package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoEnergyDevice 래퍼 — 주행 모드 및 순간 전비 읽기.
 *
 * 주요 메서드:
 *   - getOperationMode(): 0=EV, 1=HEV, 2=SPORT, -1=조회 불가
 *   - getInstantElecConValue(): 순간 전비 kWh/100km (float)
 */
class EnergyModeReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "EnergyModeReader"
        private const val ENERGY_CLASS = "android.hardware.bydauto.energy.BYDAutoEnergyDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "EnergyDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "EnergyDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "EnergyDevice 로딩 실패")
    }

    /**
     * 주행 모드. 0=EV, 1=HEV, 2=SPORT, -1=조회 불가
     * BYD SDK OperationMode: EV_MODE=0, HEV_MODE=1, SPORT_MODE=2
     */
    fun getOperationMode(): Int =
        runCatching {
            val d = device ?: return -1
            d.javaClass.getMethod("getOperationMode").invoke(d) as Int
        }.getOrDefault(-1)

    /** 순간 전비 kWh/100km. -1.0=조회 불가 */
    fun getInstantElecCon(): Double =
        runCatching {
            val d = device ?: return -1.0
            val raw = d.javaClass.getMethod("getInstantElecConValue").invoke(d)
            when (raw) {
                is Float -> raw.toDouble()
                is Double -> raw
                is Int -> raw.toDouble()
                else -> -1.0
            }.let { if (it < 0 || it > 200) -1.0 else it }
        }.getOrDefault(-1.0)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(ENERGY_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

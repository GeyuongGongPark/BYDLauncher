package com.bydlauncher.camping.sdk

import android.content.Context
import android.util.Log

/**
 * BYDAutoChargingDevice reflection 래퍼.
 * 충전 커넥터 연결 여부 및 실시간 충전 전력을 조회한다.
 *
 * CHARGING_GUN_STATE_CONNECTED_NONE = 1 → 미연결
 * CHARGING_GUN_STATE_CONNECTED_AC   = 2 → AC 충전 연결
 * CHARGING_GUN_STATE_CONNECTED_DC   = 3 → DC 충전 연결
 */
class ChargingReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "ChargingReader"
        private const val CHARGING_CLASS = "android.hardware.bydauto.charging.BYDAutoChargingDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
        private const val GUN_STATE_NOT_CONNECTED = 1
    }

    fun connect() {
        if (device != null) return

        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "ChargingDevice 로딩 성공 (시스템)"); return }

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
                Log.i(TAG, "ChargingDevice 로딩 성공: $pkg")
            }.onSuccess { return }
        }
        Log.w(TAG, "ChargingDevice 로딩 실패 — 충전 감시 비활성")
    }

    /** 충전 커넥터 연결 여부. */
    fun isConnected(): Boolean =
        runCatching {
            val d = device ?: return false
            val state = d.javaClass.getMethod("getChargingGunState").invoke(d) as Int
            state != GUN_STATE_NOT_CONNECTED
        }.getOrDefault(false)

    /** 실시간 충전 전력 (kW). 조회 불가 시 0.0 */
    fun getChargingPowerKw(): Double =
        runCatching {
            val d = device ?: return 0.0
            val power = d.javaClass.getMethod("getChargingPower").invoke(d) as Double
            if (power < 0.0) 0.0 else power
        }.getOrDefault(0.0)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(CHARGING_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

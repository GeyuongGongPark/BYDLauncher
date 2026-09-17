package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoAcDevice 읽기 전용 래퍼.
 * AcController(캠핑 모드)와 동일한 싱글턴을 공유하므로 중복 연결 없음.
 *
 * 읽기 메서드:
 *   - getAcStartState(): 1=on, 0=off
 *   - getTemprature(1): 운전석 설정 온도 (zone 1, BYD typo: Temprature)
 *   - getAcWindLevel(): 풍량 단계 (1~7)
 */
class AcReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "AcReader"
        private const val AC_CLASS = "android.hardware.bydauto.ac.BYDAutoAcDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "AcDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "AcDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "AcDevice 로딩 실패")
    }

    fun isConnected(): Boolean = device != null

    /** AC on/off 상태. 1=on, 0=off, -1=조회 불가 */
    fun getAcOn(): Boolean =
        runCatching {
            val d = device ?: return false
            (d.javaClass.getMethod("getAcStartState").invoke(d) as Int) == 1
        }.getOrDefault(false)

    /**
     * 운전석 설정 온도 (°C). -1=조회 불가.
     * zone=1: 운전석(단일존 포함)
     */
    fun getSetTemp(): Int =
        runCatching {
            val d = device ?: return -1
            val raw = d.javaClass.getMethod("getTemprature", Int::class.java).invoke(d, 1) as Int
            if (raw < 16 || raw > 32) -1 else raw
        }.getOrDefault(-1)

    /** 풍량 단계 (1~7). -1=조회 불가 */
    fun getWindLevel(): Int =
        runCatching {
            val d = device ?: return -1
            val raw = d.javaClass.getMethod("getAcWindLevel").invoke(d) as Int
            if (raw < 1 || raw > 7) -1 else raw
        }.getOrDefault(-1)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(AC_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

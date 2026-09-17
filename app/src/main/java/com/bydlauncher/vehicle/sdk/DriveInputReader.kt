package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoSpeedDevice 래퍼 — 페달 입력 및 회생제동 상태 읽기.
 *
 * 주요 메서드:
 *   - getAccelerateDeepness(): 가속 페달 깊이 0~100
 *   - getBrakeDeepness(): 브레이크 페달 깊이 0~100
 *   - getPowerGenerationState(): 회생제동 활성 1/비활성 0
 */
class DriveInputReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "DriveInputReader"
        private const val SPEED_CLASS = "android.hardware.bydauto.speed.BYDAutoSpeedDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "SpeedDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "SpeedDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "SpeedDevice 로딩 실패")
    }

    /** 가속 페달 깊이 0~100. -1=조회 불가 */
    fun getAccelerateDeepness(): Int =
        runCatching {
            val d = device ?: return -1
            (d.javaClass.getMethod("getAccelerateDeepness").invoke(d) as Int).coerceIn(-1, 100)
        }.getOrDefault(-1)

    /** 브레이크 페달 깊이 0~100. -1=조회 불가 */
    fun getBrakeDeepness(): Int =
        runCatching {
            val d = device ?: return -1
            (d.javaClass.getMethod("getBrakeDeepness").invoke(d) as Int).coerceIn(-1, 100)
        }.getOrDefault(-1)

    /** 회생제동 활성 여부 */
    fun isRegenActive(): Boolean =
        runCatching {
            val d = device ?: return false
            (d.javaClass.getMethod("getPowerGenerationState").invoke(d) as Int) == 1
        }.getOrDefault(false)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(SPEED_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

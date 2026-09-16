package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoSpeedDevice 래퍼.
 * 현재 차속을 조회하여 주행 중 여부를 판단한다.
 */
class SpeedReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "SpeedReader"
        private const val CLASS = "android.hardware.bydauto.speed.BYDAutoSpeedDevice"
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

    /** 현재 차속 (km/h, 0.0~282.0). 조회 불가 시 0.0 */
    fun getCurrentSpeedKmh(): Double =
        runCatching {
            val d = device ?: return 0.0
            val speed = d.javaClass.getMethod("getCurrentSpeed").invoke(d) as Double
            if (speed >= 0.0) speed else 0.0
        }.getOrDefault(0.0)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

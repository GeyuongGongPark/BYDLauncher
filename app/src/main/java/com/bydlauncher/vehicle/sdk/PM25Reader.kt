package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoPM2p5Device 래퍼.
 * 실내/실외 PM2.5 수치 및 공기질 등급을 조회한다.
 *
 * 등급: 1=매우좋음, 2=좋음, 3=경도오염, 4=중등도, 5=심각, 6=매우심각
 */
class PM25Reader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "PM25Reader"
        private const val CLASS = "android.hardware.bydauto.pm2p5.BYDAutoPM2p5Device"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "PM2p5Device 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "PM2p5Device 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "PM2p5Device 로딩 실패")
    }

    /** 실내 PM2.5 수치 (μg/m³). 조회 불가 시 -1 */
    fun getIndoorValue(): Int = getValue(index = 0)

    /** 실외 PM2.5 수치 (μg/m³). 조회 불가 시 -1 */
    fun getOutdoorValue(): Int = getValue(index = 1)

    /** 실내 공기질 등급 (1~6). 0 = 조회 불가 */
    fun getIndoorLevel(): Int = getLevel(index = 0)

    private fun getValue(index: Int): Int =
        runCatching {
            val d = device ?: return -1
            val arr = d.javaClass.getMethod("getPM2p5Value").invoke(d) as IntArray
            val v = arr[index]
            if (v < 0) -1 else v
        }.getOrDefault(-1)

    private fun getLevel(index: Int): Int =
        runCatching {
            val d = device ?: return 0
            val arr = d.javaClass.getMethod("getPM2p5Level").invoke(d) as IntArray
            val v = arr[index]
            if (v in 1..6) v else 0
        }.getOrDefault(0)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

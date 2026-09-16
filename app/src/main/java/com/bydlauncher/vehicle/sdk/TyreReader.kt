package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoTyreDevice 래퍼.
 * 타이어 압력 이상 및 공기 누출 상태를 조회한다.
 */
class TyreReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "TyreReader"
        private const val CLASS = "android.hardware.bydauto.tyre.BYDAutoTyreDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )

        private val TYRE_NAMES = mapOf(
            1 to "좌전 타이어",
            2 to "우전 타이어",
            3 to "좌후 타이어",
            4 to "우후 타이어",
        )
        // 상태 상수 (문서 기준)
        private const val PRESSURE_NORMAL = 0
        private const val LEAK_NORMAL = 0
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "TyreDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "TyreDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "TyreDevice 로딩 실패")
    }

    /**
     * 이상 있는 타이어 경고 문자열 목록.
     * 압력 이상 또는 공기 누출 감지 시 해당 타이어명 + 상태 추가.
     */
    fun getTyreAlerts(): List<String> {
        val d = device ?: return emptyList()
        val alerts = mutableListOf<String>()

        for ((area, name) in TYRE_NAMES) {
            runCatching {
                val pressure = d.javaClass.getMethod("getTyrePressureState", Int::class.java).invoke(d, area) as Int
                val leak = d.javaClass.getMethod("getTyreAirLeakState", Int::class.java).invoke(d, area) as Int

                when {
                    leak == 1 -> alerts.add("$name 급속 누출")
                    leak == 2 -> alerts.add("$name 서서히 누출")
                    pressure == 1 -> alerts.add("$name 압력 과다")
                    pressure == 2 -> alerts.add("$name 압력 부족")
                }
            }
        }
        return alerts
    }

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

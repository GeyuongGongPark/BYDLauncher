package com.bydlauncher.camping.sdk

import android.content.Context
import android.util.Log

/**
 * BYDAutoGearboxDevice reflection 래퍼.
 * 캠핑 모드 시작 전 P단 여부를 확인하는 데 사용한다.
 *
 * GEARBOX_AUTO_MODE_P = 1 → 주차(P)
 */
class GearReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "GearReader"
        private const val GEARBOX_CLASS = "android.hardware.bydauto.gearbox.BYDAutoGearboxDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
        private const val GEARBOX_AUTO_MODE_P = 1
        private const val GEARBOX_AUTO_MODE_N = 5
        private const val GEARBOX_AUTO_MODE_D = 6
        private const val GEARBOX_AUTO_MODE_R = 7
        private const val GEARBOX_AUTO_MODE_S = 8
    }

    enum class GearState { PARK, NEUTRAL, DRIVE, REVERSE, SPORT, UNKNOWN }

    fun connect() {
        if (device != null) return

        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "GearboxDevice 로딩 성공 (시스템)"); return }

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
                Log.i(TAG, "GearboxDevice 로딩 성공: $pkg")
            }.onSuccess { return }
        }
        Log.w(TAG, "GearboxDevice 로딩 실패 — 기어 체크 비활성")
    }

    /** 현재 기어 상태. SDK 로딩 실패 시 UNKNOWN 반환 */
    fun getCurrentGear(): GearState =
        runCatching {
            val d = device ?: return GearState.UNKNOWN
            when (d.javaClass.getMethod("getGearboxAutoModeType").invoke(d) as Int) {
                GEARBOX_AUTO_MODE_P -> GearState.PARK
                GEARBOX_AUTO_MODE_N -> GearState.NEUTRAL
                GEARBOX_AUTO_MODE_D -> GearState.DRIVE
                GEARBOX_AUTO_MODE_R -> GearState.REVERSE
                GEARBOX_AUTO_MODE_S -> GearState.SPORT
                else -> GearState.UNKNOWN
            }
        }.getOrDefault(GearState.UNKNOWN)

    /**
     * P단(주차) 여부.
     * SDK 로딩 실패 시 true 반환 (안전하게 허용).
     */
    fun isParked(): Boolean =
        runCatching {
            val d = device ?: return true
            val mode = d.javaClass.getMethod("getGearboxAutoModeType").invoke(d) as Int
            mode == GEARBOX_AUTO_MODE_P
        }.getOrDefault(true)

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(GEARBOX_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

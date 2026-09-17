package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoBodyworkDevice 래퍼.
 * 도어/윈도우 열림 상태를 조회한다.
 */
class BodyworkReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "BodyworkReader"
        private const val CLASS = "android.hardware.bydauto.bodywork.BYDAutoBodyworkDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )

        // 도어 영역 상수 (문서 기준)
        private val DOOR_AREAS = mapOf(
            1 to "운전석 도어",
            2 to "조수석 도어",
            3 to "좌후방 도어",
            4 to "우후방 도어",
            5 to "보닛",
            6 to "트렁크",
        )
        // 윈도우 영역 상수
        private val WINDOW_AREAS = mapOf(
            1 to "운전석 창문",
            2 to "조수석 창문",
            3 to "좌후방 창문",
            4 to "우후방 창문",
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "BodyworkDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "BodyworkDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "BodyworkDevice 로딩 실패")
    }

    /**
     * 열려 있는 도어/윈도우 이름 목록.
     * 도어 state != 0이면 열림, 윈도우 개방률 > 5%이면 열림으로 판단.
     */
    fun getOpenDoors(): List<String> {
        val d = device ?: return emptyList()
        val result = mutableListOf<String>()

        for ((area, name) in DOOR_AREAS) {
            runCatching {
                val state = d.javaClass.getMethod("getDoorState", Int::class.java).invoke(d, area) as Int
                if (state != 0) result.add(name)
            }
        }
        for ((area, name) in WINDOW_AREAS) {
            runCatching {
                val pct = d.javaClass.getMethod("getWindowOpenPercent", Int::class.java).invoke(d, area) as Int
                if (pct > 5) result.add(name)
            }
        }
        return result
    }

    /** 창문 개방률 맵. area 1~4 → % (0~100). 조회 실패 시 해당 area 제외 */
    fun getWindowPercents(): Map<Int, Int> {
        val d = device ?: return emptyMap()
        val result = mutableMapOf<Int, Int>()
        for (area in 1..4) {
            runCatching {
                val pct = d.javaClass.getMethod("getWindowOpenPercent", Int::class.java).invoke(d, area) as Int
                result[area] = pct.coerceIn(0, 100)
            }
        }
        return result
    }

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

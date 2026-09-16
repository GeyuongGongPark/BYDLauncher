package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoInstrumentDevice 래퍼.
 * 주요 고장 코드를 조회하여 활성 경고 목록을 반환한다.
 */
class MalfunctionReader(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    companion object {
        private const val TAG = "MalfunctionReader"
        private const val CLASS = "android.hardware.bydauto.instrument.BYDAutoInstrumentDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )

        // 확인할 고장 유형 (typeName 상수 값 → 표시 이름)
        private val MALFUNCTION_TYPES = mapOf(
            5  to "엔진 이상",
            6  to "ABS 이상",
            7  to "ESP 이상",
            11 to "에어백(SRS) 이상",
            12 to "전동 조향(EPS) 이상",
            13 to "타이어 압력 센서",
            15 to "모터 과열",
            16 to "배터리 이상",
            17 to "배터리 과열",
            18 to "동력 시스템 이상",
            20 to "EV 시스템 이상",
        )
    }

    fun connect() {
        if (device != null) return
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "InstrumentDevice 로딩 성공 (시스템)"); return }
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
            }.onSuccess { Log.i(TAG, "InstrumentDevice 로딩 성공: $pkg"); return }
        }
        Log.w(TAG, "InstrumentDevice 로딩 실패")
    }

    /** 활성 고장 이름 목록. 정상이면 빈 리스트 */
    fun getMalfunctions(): List<String> {
        val d = device ?: return emptyList()
        val result = mutableListOf<String>()

        for ((typeId, name) in MALFUNCTION_TYPES) {
            runCatching {
                val state = d.javaClass.getMethod("getMalfunctionInfo", Int::class.java).invoke(d, typeId) as Int
                if (state != 0) result.add(name)
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

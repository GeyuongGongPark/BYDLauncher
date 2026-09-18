package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoSettingDevice 시트 메모리 래퍼.
 *
 * BydController 앱(v2.1.1) 역공학으로 확인된 실제 API:
 *   - saveSeatParamsAll(slot: Int, zone: Int): Int  → 현재 시트 포지션을 슬롯에 저장
 *   - resetSeatParams(slot: Int, zone: Int)         → 슬롯에 저장된 포지션으로 복원
 *
 * slot: 1 = 메모리 1, 2 = 메모리 2
 * zone: 1 = 운전석 (DRIVER)
 *
 * saveSeatParamsAll 반환값: 음수 또는 65535이면 오류
 * 씨라이언 7 플러스 / DiLink 5.0 전용 기능.
 */
class SeatController(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    /** SDK 로딩 성공 여부 */
    val isAvailable: Boolean get() = device != null

    companion object {
        private const val TAG = "SeatController"
        private const val SETTING_CLASS = "android.hardware.bydauto.setting.BYDAutoSettingDevice"
        private const val DRIVER_ZONE = 1

        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar",
            "com.byd.scenemode", "com.byd.autoservice",
        )
    }

    fun connect() {
        if (device != null) return

        // DiLink 5.0: 시스템 ClassLoader
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "SettingDevice 로딩 성공 (시스템)"); return }

        // DiLink 3.0: 외부 SDK APK
        for (pkg in SDK_PACKAGES) {
            if (device != null) break
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir, ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir, ctx.classLoader,
                )
                device = getInstance(cl)
                Log.i(TAG, "SettingDevice 로딩 성공: $pkg")
            }
        }

        if (device == null) {
            Log.w(TAG, "SettingDevice 없음 — 씨라이언 7 플러스(DiLink 5.0) 전용 기능")
        }
    }

    /**
     * 현재 시트 포지션을 지정 슬롯에 저장.
     * @param slot 1 또는 2
     * @return 성공 시 true
     */
    fun saveSlot(slot: Int): Boolean {
        require(slot == 1 || slot == 2) { "slot은 1 또는 2여야 합니다" }
        val d = device ?: return false
        return runCatching {
            val result = d.javaClass
                .getMethod("saveSeatParamsAll", Int::class.java, Int::class.java)
                .invoke(d, slot, DRIVER_ZONE) as Int
            val ok = result >= 0 && result != 65535
            Log.i(TAG, "saveSlot($slot) → result=$result ok=$ok")
            ok
        }.getOrElse {
            Log.w(TAG, "saveSlot($slot) 실패: ${it.message}")
            false
        }
    }

    /**
     * 지정 슬롯의 포지션으로 시트 복원.
     * @param slot 1 또는 2
     * @return 성공 시 true
     */
    fun recallSlot(slot: Int): Boolean {
        require(slot == 1 || slot == 2) { "slot은 1 또는 2여야 합니다" }
        val d = device ?: return false
        return runCatching {
            d.javaClass
                .getMethod("resetSeatParams", Int::class.java, Int::class.java)
                .invoke(d, slot, DRIVER_ZONE)
            Log.i(TAG, "recallSlot($slot) 완료")
            true
        }.getOrElse {
            Log.w(TAG, "recallSlot($slot) 실패: ${it.message}")
            false
        }
    }

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(SETTING_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

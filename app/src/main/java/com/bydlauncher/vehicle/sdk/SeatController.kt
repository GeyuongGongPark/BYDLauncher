package com.bydlauncher.vehicle.sdk

import android.content.Context
import android.util.Log
import com.bydlauncher.camping.sdk.VehicleContextWrapper

/**
 * BYDAutoSeatDevice reflection 탐색 래퍼.
 *
 * BYD DiLink에 시트 제어 API가 있는지 미확인이므로,
 * connect() 시 클래스 로딩을 시도하고 성공하면 전체 메서드 목록을 로그에 출력한다.
 * 실차에서 logcat(tag=SeatController)으로 사용 가능한 메서드를 확인한 뒤
 * 아래 후보 목록에 올바른 이름을 추가하면 자동으로 사용된다.
 *
 * 탐색 대상: android.hardware.bydauto.seat.BYDAutoSeatDevice
 * zone: 1 = 운전석
 */
class SeatController(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var device: Any? = null

    private var getForeAftSpec: MethodSpec? = null
    private var setForeAftSpec: MethodSpec? = null

    /** SDK 로딩 + setter 메서드 발견 시 true */
    val isAvailable: Boolean get() = device != null && setForeAftSpec != null

    /** SDK 로딩 + getter 메서드 발견 시 true */
    val canRead: Boolean get() = device != null && getForeAftSpec != null

    /** SDK 클래스가 기기에 존재하는지 여부 (메서드 매칭 여부와 무관) */
    val isClassFound: Boolean get() = device != null

    private data class MethodSpec(val name: String, val hasZoneArg: Boolean)

    companion object {
        private const val TAG = "SeatController"
        private const val SEAT_CLASS = "android.hardware.bydauto.seat.BYDAutoSeatDevice"
        private const val DRIVER_ZONE = 1

        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )

        // 앞뒤 getter 후보 (zone 인자 있는 것 먼저, 없는 것 다음)
        private val GET_ZONE_CANDIDATES = listOf(
            "getSeatForeAft", "getSeatSlidePosition", "getSeatPosition",
            "getSeatForwardBackward", "getSeatFowardBackward",
            "getSeatFrontBack", "getSeatSlide", "getDriverSeatPosition",
        )
        private val GET_NO_ZONE_CANDIDATES = listOf(
            "getDriverSeatForeAft", "getSeatForeAft", "getDriverSeatPosition",
        )

        // 앞뒤 setter 후보 (zone+value, value만)
        private val SET_ZONE_VALUE_CANDIDATES = listOf(
            "setSeatForeAft", "setSeatSlidePosition", "setSeatPosition",
            "setSeatForwardBackward", "setSeatFowardBackward",
            "setSeatFrontBack", "setSeatSlide", "setDriverSeatPosition",
        )
        private val SET_VALUE_CANDIDATES = listOf(
            "setDriverSeatForeAft", "setSeatForeAft", "setDriverSeatPosition",
        )
    }

    fun connect() {
        if (device != null) return

        // DiLink 5.0: 시스템 ClassLoader
        runCatching { device = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "SeatDevice 로딩 성공 (시스템)") }

        // DiLink 3.0: 외부 SDK APK
        if (device == null) {
            for (pkg in SDK_PACKAGES) {
                if (device != null) break
                runCatching {
                    val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                    val cl = dalvik.system.DexClassLoader(
                        info.sourceDir, ctx.codeCacheDir.absolutePath,
                        info.nativeLibraryDir, ctx.classLoader,
                    )
                    device = getInstance(cl)
                    Log.i(TAG, "SeatDevice 로딩 성공: $pkg")
                }
            }
        }

        if (device == null) {
            Log.w(TAG, "SeatDevice 클래스 없음 — SDK 미탑재 또는 미지원 차종")
            return
        }

        // 실차 분석용: 전체 공개 메서드 목록 출력
        logAllMethods()
        detectMethods()
    }

    /** 현재 앞뒤 포지션 (0~100, 100=최대 뒤). 읽기 불가 시 -1 */
    fun getCurrentForeAft(): Int {
        val d = device ?: return -1
        val spec = getForeAftSpec ?: return -1
        return runCatching {
            val raw = if (spec.hasZoneArg) {
                d.javaClass.getMethod(spec.name, Int::class.java).invoke(d, DRIVER_ZONE)
            } else {
                d.javaClass.getMethod(spec.name).invoke(d)
            }
            (raw as? Int)?.coerceIn(0, 100) ?: -1
        }.getOrDefault(-1)
    }

    /**
     * 앞뒤 포지션 설정 (0~100). 성공 시 true.
     * 안전: 호출 전 반드시 P기어 확인 후 호출할 것.
     */
    fun setForeAft(value: Int): Boolean {
        val d = device ?: return false
        val spec = setForeAftSpec ?: return false
        val clamped = value.coerceIn(0, 100)
        return runCatching {
            if (spec.hasZoneArg) {
                d.javaClass.getMethod(spec.name, Int::class.java, Int::class.java)
                    .invoke(d, DRIVER_ZONE, clamped)
            } else {
                d.javaClass.getMethod(spec.name, Int::class.java).invoke(d, clamped)
            }
            Log.i(TAG, "setForeAft(zone=$DRIVER_ZONE, value=$clamped) 호출")
            true
        }.getOrDefault(false)
    }

    /** 하차 편의 포지션: 시트 최대 뒤로 (foreAft=100) */
    fun moveToEntryPosition(): Boolean {
        Log.i(TAG, "moveToEntryPosition() — 시트 최대 뒤로")
        return setForeAft(100)
    }

    /** 드라이빙 포지션 복원 */
    fun restorePosition(foreAft: Int): Boolean {
        Log.i(TAG, "restorePosition(foreAft=$foreAft)")
        return setForeAft(foreAft)
    }

    // --- private ---

    /** 실차 분석용: 전체 공개 메서드를 logcat에 출력 */
    private fun logAllMethods() {
        val d = device ?: return
        Log.i(TAG, "=== SeatDevice 전체 메서드 목록 ===")
        d.javaClass.methods
            .filter { it.declaringClass != Object::class.java }
            .sortedBy { it.name }
            .forEach { m ->
                val params = m.parameterTypes.joinToString { it.simpleName }
                val ret = m.returnType.simpleName
                Log.i(TAG, "  $ret ${m.name}($params)")
            }
        Log.i(TAG, "=== 목록 끝 ===")
    }

    /** getter/setter 메서드명 자동 탐색 */
    private fun detectMethods() {
        val d = device ?: return

        // getter: zone 인자 있는 것
        for (name in GET_ZONE_CANDIDATES) {
            if (getForeAftSpec != null) break
            runCatching {
                d.javaClass.getMethod(name, Int::class.java).invoke(d, DRIVER_ZONE).also { result ->
                    Log.i(TAG, "✅ getter 발견: $name(zone=$DRIVER_ZONE) = $result")
                    getForeAftSpec = MethodSpec(name, true)
                }
            }
        }
        // getter: 인자 없는 것
        if (getForeAftSpec == null) {
            for (name in GET_NO_ZONE_CANDIDATES) {
                runCatching {
                    d.javaClass.getMethod(name).invoke(d).also { result ->
                        Log.i(TAG, "✅ getter 발견: $name() = $result")
                        if (getForeAftSpec == null) getForeAftSpec = MethodSpec(name, false)
                    }
                }
            }
        }

        // setter: zone + value (메서드 존재 여부만 확인, invoke 안 함)
        for (name in SET_ZONE_VALUE_CANDIDATES) {
            if (setForeAftSpec != null) break
            runCatching {
                d.javaClass.getMethod(name, Int::class.java, Int::class.java)
                Log.i(TAG, "✅ setter 발견: $name(zone, value)")
                setForeAftSpec = MethodSpec(name, true)
            }
        }
        // setter: value 만
        if (setForeAftSpec == null) {
            for (name in SET_VALUE_CANDIDATES) {
                runCatching {
                    d.javaClass.getMethod(name, Int::class.java)
                    Log.i(TAG, "✅ setter 발견: $name(value)")
                    if (setForeAftSpec == null) setForeAftSpec = MethodSpec(name, false)
                }
            }
        }

        Log.i(TAG, "탐색 결과 — getter=${getForeAftSpec?.name} setter=${setForeAftSpec?.name} isAvailable=$isAvailable")
    }

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(SEAT_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }
}

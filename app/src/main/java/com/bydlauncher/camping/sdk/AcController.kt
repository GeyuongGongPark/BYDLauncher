package com.bydlauncher.camping.sdk

import android.content.Context
import android.util.Log

/**
 * BYDAutoAcDevice reflection 래퍼.
 *
 * DiLink 5.0 (씨라이언7): 시스템 ClassLoader에 이미 존재
 * DiLink 3.0 (아토3/돌핀): com.byd.hvac 등 SDK APK에서 DexClassLoader로 로딩
 *
 * API 레퍼런스: wheregoes/byd-dolphin-hacking — 실차 검증 완료
 *   - start(0): AC on
 *   - stop(0): AC off
 *   - setAcTemperature(zone=1, celsius, source=1, param4=1): 온도 설정 (source=1 필수)
 *   - getAcStartState(): 1=on, 0=off
 *   - getTemprature(4): 외부 온도 (typo: Temprature)
 */
class AcController(context: Context) {

    private val ctx = VehicleContextWrapper(context)
    private var acDevice: Any? = null

    companion object {
        private const val TAG = "AcController"
        private const val AC_CLASS = "android.hardware.bydauto.ac.BYDAutoAcDevice"
        private val SDK_PACKAGES = listOf(
            "com.byd.hvac", "com.byd.carsettings", "com.byd.mycar", "com.byd.scenemode"
        )
    }

    /**
     * SDK를 로딩하고 AcDevice 인스턴스를 획득한다.
     * 실패 시 IllegalStateException.
     */
    fun connect() {
        if (acDevice != null) return

        // DiLink 5.0: 시스템 ClassLoader
        runCatching { acDevice = getInstance(ctx.classLoader) }
            .onSuccess { Log.i(TAG, "DiLink 5.0 SDK 로딩 성공"); return }

        // DiLink 3.0: 외부 SDK APK
        for (pkg in SDK_PACKAGES) {
            runCatching {
                val info = ctx.packageManager.getApplicationInfo(pkg, 0)
                val cl = dalvik.system.DexClassLoader(
                    info.sourceDir,
                    ctx.codeCacheDir.absolutePath,
                    info.nativeLibraryDir,
                    ctx.classLoader,
                )
                acDevice = getInstance(cl)
                Log.i(TAG, "DiLink 3.0 SDK 로딩 성공: $pkg")
            }.onSuccess { return }
        }

        throw IllegalStateException("BYD AC SDK 없음 — 지원되지 않는 차종")
    }

    fun isConnected(): Boolean = acDevice != null

    /** 에어컨 켜기 */
    fun start() {
        invoke("start", Int::class.java, 0)
        Log.i(TAG, "start() 호출")
    }

    /** 에어컨 끄기 */
    fun stop() {
        runCatching { invoke("stop", Int::class.java, 0) }
        Log.i(TAG, "stop() 호출")
    }

    /**
     * 온도 설정. source=1(voice), param4=1 필수.
     * source=0(UI_KEY)이면 INVALID_VALUE 반환됨 (실차 확인).
     */
    fun setTemperature(celsius: Int) {
        val ac = acDevice ?: return
        runCatching {
            ac.javaClass.getMethod(
                "setAcTemperature",
                Int::class.java, Int::class.java, Int::class.java, Int::class.java,
            ).invoke(ac, 1, celsius, 1, 1)
        }.onFailure { Log.w(TAG, "setTemperature 실패: ${it.message}") }
        Log.i(TAG, "setTemperature($celsius°C) 호출")
    }

    /** 에어컨 현재 상태. 1=on, 0=off, -1=조회 불가 */
    fun getStartState(): Int =
        runCatching {
            val ac = acDevice ?: return -1
            ac.javaClass.getMethod("getAcStartState").invoke(ac) as Int
        }.getOrDefault(-1)

    /** 외부 기온 (°C). -1=조회 불가 */
    fun getOutsideTemp(): Int =
        runCatching {
            val ac = acDevice ?: return -1
            // BYD typo: getTemprature (e 빠짐)
            val raw = ac.javaClass.getMethod("getTemprature", Int::class.java).invoke(ac, 4) as Int
            if (raw < -50 || raw > 80) -1 else raw
        }.getOrDefault(-1)

    fun disconnect() {
        acDevice = null
    }

    // --- private ---

    private fun getInstance(cl: ClassLoader): Any {
        val cls = cl.loadClass(AC_CLASS)
        return cls.getMethod("getInstance", Context::class.java).invoke(null, ctx)
            ?: error("getInstance returned null")
    }

    private fun invoke(method: String, paramType: Class<*>, arg: Any): Any? {
        val ac = acDevice ?: error("connect() 먼저 호출")
        return ac.javaClass.getMethod(method, paramType).invoke(ac, arg)
    }
}

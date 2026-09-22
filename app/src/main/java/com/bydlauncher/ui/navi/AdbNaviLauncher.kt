package com.bydlauncher.ui.navi

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.MotionEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ADB 없이 ActivityOptions.setLaunchDisplayId()로 네비 앱을 VirtualDisplay에서 실행.
 * 터치는 InputManager.injectInputEvent() reflection으로 주입.
 */
object AdbNaviLauncher {

    private const val TAG = "AdbNaviLauncher"

    /**
     * 네비 앱을 지정된 VirtualDisplay에서 실행.
     * ActivityOptions.setLaunchDisplayId() 사용 — ADB 불필요.
     */
    suspend fun launch(
        context: Context,
        packageName: String,
        displayId: Int,
    ): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching<Unit> {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: error("$packageName 실행 가능한 Activity 없음")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

            val options = ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
            context.startActivity(intent, options.toBundle())
            Log.i(TAG, "네비 실행 성공: $packageName → display $displayId")
        }.onFailure {
            Log.w(TAG, "네비 실행 실패: ${it.message}")
        }
    }

    /**
     * VirtualDisplay에 터치 이벤트 주입.
     * InputManager.injectInputEvent() hidden API 호출.
     */
    suspend fun injectTouch(
        context: Context,
        displayId: Int,
        action: Int,
        x: Int,
        y: Int,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val imClass = Class.forName("android.hardware.input.InputManager")
            val im = imClass.getMethod("getInstance").invoke(null)
            val now = android.os.SystemClock.uptimeMillis()
            val event = MotionEvent.obtain(now, now, action, x.toFloat(), y.toFloat(), 0).apply {
                source = InputDevice.SOURCE_TOUCHSCREEN
            }
            runCatching {
                MotionEvent::class.java.getMethod("setDisplayId", Int::class.java)
                    .invoke(event, displayId)
            }
            imClass.getMethod("injectInputEvent", InputEvent::class.java, Int::class.java)
                .invoke(im, event, 0)
            event.recycle()
        }
    }
}

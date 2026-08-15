package com.bydlauncher.ui.navi

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * VirtualDisplay를 이용한 네비 앱 임베딩 시도.
 *
 * 동작 조건:
 * - BYDLauncher가 /system/priv-app/ 에 설치되어 있거나
 * - ADB로 INTERNAL_SYSTEM_WINDOW / MANAGE_ACTIVITY_TASKS 권한 부여 시
 *
 * 조건 불만족 시 [onEmbeddingFailed] 콜백이 호출되어 fallback UI로 전환됨.
 */
@Composable
fun EmbeddedNaviView(
    packageName: String,
    modifier: Modifier = Modifier,
    onEmbeddingFailed: () -> Unit,
) {
    var virtualDisplayHolder by remember { mutableStateOf<android.hardware.display.VirtualDisplay?>(null) }

    DisposableEffect(packageName) {
        onDispose {
            virtualDisplayHolder?.release()
            virtualDisplayHolder = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).also { sv ->
                sv.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        val launched = tryLaunchOnVirtualDisplay(
                            context = context,
                            packageName = packageName,
                            surface = holder.surface,
                            width = sv.width.coerceAtLeast(1),
                            height = sv.height.coerceAtLeast(1),
                            density = context.resources.displayMetrics.densityDpi,
                            onDisplayCreated = { vd -> virtualDisplayHolder = vd },
                        )
                        if (!launched) onEmbeddingFailed()
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) = Unit
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        virtualDisplayHolder?.release()
                        virtualDisplayHolder = null
                    }
                })
            }
        },
    )
}

/**
 * VirtualDisplay 생성 + reflection으로 setLaunchDisplayId 호출.
 * 실패(SecurityException, NoSuchMethodException 등) 시 false 반환.
 */
private fun tryLaunchOnVirtualDisplay(
    context: Context,
    packageName: String,
    surface: android.view.Surface,
    width: Int,
    height: Int,
    density: Int,
    onDisplayCreated: (android.hardware.display.VirtualDisplay) -> Unit,
): Boolean = try {
    val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

    @Suppress("DEPRECATION")
    val vd = dm.createVirtualDisplay(
        "BYDNaviDisplay",
        width,
        height,
        density,
        surface,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
    )
    onDisplayCreated(vd)

    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        ?: return false
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    // Hidden API: ActivityOptions.setLaunchDisplayId(int)
    val opts = ActivityOptions.makeBasic()
    val method = ActivityOptions::class.java.getDeclaredMethod("setLaunchDisplayId", Int::class.java)
    method.isAccessible = true
    method.invoke(opts, vd.display.displayId)

    context.startActivity(intent, opts.toBundle())
    true
} catch (e: SecurityException) {
    false
} catch (e: NoSuchMethodException) {
    false
} catch (e: Exception) {
    false
}

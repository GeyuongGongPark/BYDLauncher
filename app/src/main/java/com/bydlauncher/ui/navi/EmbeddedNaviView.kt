package com.bydlauncher.ui.navi

import android.content.Context
import android.hardware.display.DisplayManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

/**
 * VirtualDisplay + ADB loopback(am start --display)으로 네비 앱 임베딩.
 *
 * 동작 조건:
 * - 차량에서 ADB TCP(포트 5555)가 활성화되어 있어야 함
 * - 개발자 옵션 > 무선 디버깅 활성화
 *
 * 실패 시 [onEmbeddingFailed] 콜백 → fallback UI로 전환.
 */
@Composable
fun EmbeddedNaviView(
    packageName: String,
    modifier: Modifier = Modifier,
    onEmbeddingFailed: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var virtualDisplay by remember { mutableStateOf<android.hardware.display.VirtualDisplay?>(null) }

    DisposableEffect(packageName) {
        onDispose {
            virtualDisplay?.release()
            virtualDisplay = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SurfaceView(ctx).also { sv ->
                sv.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        val vd = createVirtualDisplay(
                            context = ctx,
                            surface = holder.surface,
                            width = sv.width.coerceAtLeast(1),
                            height = sv.height.coerceAtLeast(1),
                            density = ctx.resources.displayMetrics.densityDpi,
                        )
                        if (vd == null) {
                            onEmbeddingFailed()
                            return
                        }
                        virtualDisplay = vd

                        scope.launch {
                            val result = AdbNaviLauncher.launch(
                                context = context,
                                packageName = packageName,
                                displayId = vd.display.displayId,
                            )
                            if (result.isFailure) {
                                vd.release()
                                virtualDisplay = null
                                onEmbeddingFailed()
                            }
                        }
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) = Unit
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        virtualDisplay?.release()
                        virtualDisplay = null
                    }
                })
            }
        },
    )
}

private fun createVirtualDisplay(
    context: Context,
    surface: android.view.Surface,
    width: Int,
    height: Int,
    density: Int,
): android.hardware.display.VirtualDisplay? = runCatching {
    val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    @Suppress("DEPRECATION")
    dm.createVirtualDisplay(
        "BYDNaviDisplay",
        width,
        height,
        density,
        surface,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
    )
}.getOrNull()
